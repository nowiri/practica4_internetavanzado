import encapsulations.*;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import services.*;

import javax.naming.CompositeName;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;

import static io.javalin.apibuilder.ApiBuilder.before;

public class Main {
    public static void main(String[] args) {

        //JAVALIN INIT
        Javalin app = Javalin.create(config ->{
            config.addStaticFiles("/public"); //STATIC FILES -> /resources/public
        }).start(7000);

        //REGISTER THYMELEAF IN JAVALIN
        JavalinRenderer.register(JavalinThymeleaf.INSTANCE, ".html");

        //DATABASE INIT
        DBStart.getInstancia().init();

        //DEFAULT USER
        UsuarioServices.getInstancia().crear(new Usuario("admin","admin","admin"));

        //for(int i=0;i<26;i++){
        //    ProductoServices.getInstancia().crear(new Producto("nombre"+i,75,"DEFAULT"));
        //}


        /***
         * default endpoint
         */
        app.get("/", ctx -> ctx.redirect("/productos/1"));

        /**
         * Login endpoints and logic
         */

        app.before("/confirmarLog", ctx -> {
            String user = ctx.formParam("Username");
            String pass = ctx.formParam("Password");
            if(UsuarioServices.getInstancia().verifyUser(pass,user) == null){
                ctx.redirect("/login.html");
            }
            //IF USER IS FOUND AND CREDENTIALS MATCH, CONTINUE
        });

        app.post("/confirmarLog", ctx -> {
            String user = ctx.formParam("Username");
            //INVALIDATE PREVIOUS SESSION
            ctx.req.getSession().invalidate();
            //ADD USER TO SESSION ATRIBUTE (MEANING ITS LOGGED)
            ctx.sessionAttribute("user", user);
            ctx.redirect("/");
        });

        app.post("/crearUsuario", ctx -> {
            //GETTING FORM PARAMS CONTAINING CREDENTIALS
            String user = ctx.formParam("Username");
            String pass = ctx.formParam("Password");
            String name = ctx.formParam("Name");

            //POJO INSTANCE
            Usuario tmp = new Usuario(user,name,pass);

            //ADDING TO DATABASE VIA ORM
            UsuarioServices.getInstancia().crear(tmp);

            ctx.redirect("/login.html");
        });

        /**
         * Products list logic using Thymeleaf
         */

        app.get("/productos/:page", ctx -> {
            EntityManager em = ProductoServices.getInstancia().getEntityManager();
            String page = ctx.pathParam("page",String.class).get();
            //get product list from database
            int pageSize = 10;
            String countQ = "Select count (p.id) from Producto p";
            Query countQuery = em.createQuery(countQ);
            Long countResults = (Long) countQuery.getSingleResult();

            int pageCant = (int) (Math.ceil(countResults / pageSize));

            int lastPageNumber;
            if(page==""){
                lastPageNumber = 1;
            }else{
                lastPageNumber = Integer.valueOf(page);
            }

            ctx.sessionAttribute("currentp", lastPageNumber);

            Query query = em.createQuery("from Producto");
            query.setFirstResult((lastPageNumber - 1) * pageSize);
            query.setMaxResults(pageSize);
            List<Producto> lista = query.getResultList();

            //List<Producto> lista = ProductoServices.getInstancia().findAll();


            Map<String, Object> modelo = new HashMap<>();
            modelo.put("pageCant",pageCant);
            modelo.put("lista",lista);

            //IF THERE'S NO LOGGED USER, THERE ARE NO ITEMS IN THE CAR.
            if(ctx.sessionAttribute("user")== null){
                modelo.put("size",0);
            }else{
                //IF THERES A LOGGED USER VERIFY IF IT HAS A CAR, IF NOT, CREATE IT WITH SIZE 0
                if(ctx.sessionAttribute("carrito")==null){

                    CarroCompra carrito = new CarroCompra(ctx.req.getSession().getId());
                    ctx.sessionAttribute("carrito",carrito);
                    modelo.put("size",0);
                }else{
                    //IF THERES AN USER AN IT HAS A CAR, GIVE ME THE SIZE
                    modelo.put("size",((CarroCompra)ctx.sessionAttribute("carrito")).cartSize());
                }
            }
            //SEND MODEL TO TEMPLATE FOR RENDERING
            ctx.render("/templates/lista_productos.html",modelo);
        });

        app.before("/gestionProd", ctx -> {
            //VERIFY IS THERE IS AN USER LOGGED
            if(ctx.sessionAttribute("user") == null){
                System.out.println("user not found");
                ctx.redirect("/login.html");
            }
            //CONTINUE REQ IS USER IS LOGGED
        });

        app.get("/gestionProd", ctx -> {
            //VERIFYING IF USER IS ADMIN, SENSITIVE INFORMATION IS DISPLAYED.
           if(ctx.sessionAttribute("user").equals("admin")){
               //GET PRODUCT LIST FROM DATABASE
               List<Producto> lista = ProductoServices.getInstancia().findAll();
               Map<String, Object> modelo = new HashMap<>();
               modelo.put("lista",lista);
               modelo.put("size",((CarroCompra)ctx.sessionAttribute("carrito")).cartSize());
               ctx.render("/templates/gestionar_producto.html",modelo);
           }else{
               //USER IS NOT ADMIN
               ctx.redirect("/error_permiso.html");
           }
        });

        /**
         * CRUD Productos
         */
        //CREATE
        app.post("/nuevoProd", ctx ->{

            //GETTING PARAMETERS
            String nombre = ctx.formParam("nombre");
            float precio = ctx.formParam("precio", Float.class).get();
            String descripcion = ctx.formParam("descripcion");
            //POJO INSTANCE
            Producto p = new Producto(nombre,precio,descripcion);
            //ADDING TO DATABASE VIA ORM
            ProductoServices.getInstancia().crear(p);

            ctx.uploadedFiles("foto").forEach(uploadedFile -> {
                try {
                    byte[] bytes = uploadedFile.getContent().readAllBytes();
                    String encodedString = Base64.getEncoder().encodeToString(bytes);
                    Foto foto = new Foto(uploadedFile.getFilename(), uploadedFile.getContentType(), encodedString,p);
                    FotoServices.getInstancia().crear(foto);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ctx.redirect("/gestionProd");

            });

        });

        //DELETE
        app.get("/eliminarProd/:id", ctx ->{
            //GETTING ID
            int id = ctx.pathParam("id",Integer.class).get();
            //REMOVING FROM DATABASE
            ProductoServices.getInstancia().eliminar(id);
            ctx.redirect("/gestionProd");
        });

        //UPDATE
        app.get("/editarProd/:id", ctx ->{
            //GETTING ID OF DESIRED PRODUCT
            int id = ctx.pathParam("id",Integer.class).get();

            Map<String, Object> modelo = new HashMap<>();

            //FIND PRODUCT IN DATABASE
            Producto p = ProductoServices.getInstancia().find(id);

            //ADDING PRODUCT INFO FOR TEMPLATE
            modelo.put("id",id);
            modelo.put("nombre",p.getNombre());
            modelo.put("precio",p.getPrecio());

            //SEND TO TEMPLATE
            ctx.render("/templates/editarProducto.html",modelo);
        });

        app.post("/editarProd/:id", ctx -> {
            //GETTING ID OF PRODUCT ABOUT TO BE EDITED
            int id = ctx.pathParam("id",Integer.class).get();
            //NEW INFO
            String nombre = ctx.formParam("nombre", String.class).get();
            float precio = ctx.formParam("precio", Float.class).get();
            //POJO INSTANCE
            Producto p = new Producto(id,nombre,precio);
            //MERGE
            ProductoServices.getInstancia().editar(p);

            ctx.redirect("/gestionProd");
        });

        /**
         * Añadir al carro
         */

        app.before("/anadirAlCarrito", ctx -> {
            //VERIFY IF THERE'S A LOGGED USER
            if(ctx.sessionAttribute("carrito") == null){
                ctx.redirect("login.html");
            }
        });

        app.post("/anadirAlCarrito", ctx ->{
            //GETTING FORM PARAMS
            int id = ctx.formParam("id", Integer.class).get();
            int cantidad = ctx.formParam("cantidad", Integer.class).get();
            //GETTING CART FROM FROM SESSION
            CarroCompra carrito = ctx.sessionAttribute("carrito");

            if(cantidad > 0){
                //CHECK IF PRODUCT EXISTS IN CART
                DetalleProducto dp = carrito.buscarProducto(id);

                if(dp==null){
                    Producto tmp = ProductoServices.getInstancia().find(id);
                    dp = new DetalleProducto(tmp,cantidad);
                    carrito.addProducto(dp);
                }else{
                    dp.setCantidad(dp.getCantidad()+cantidad);
                }

            }
            ctx.redirect("/productos/"+ctx.sessionAttribute("currentp"));
        });

        app.before("/carrito", ctx -> {
            if(ctx.sessionAttribute("carrito") == null){
                ctx.redirect("login.html");
            }
        });

        app.get("/carrito", ctx -> {
            //VERIFY IF THERE'S A LOGGED USER
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("lista",carrito.getListaProductos());
            modelo.put("size",((CarroCompra)ctx.sessionAttribute("carrito")).cartSize());
            modelo.put("user",ctx.sessionAttribute("user"));
            ctx.render("/templates/micarrito.html",modelo);
        });

        app.post("/eliminarProdCarrito/:id",ctx ->{

            int cantidad = ctx.formParam("cantidad", Integer.class).get();
            int id = ctx.pathParam("id",Integer.class).get();
            CarroCompra carrito = ctx.sessionAttribute("carrito");

            DetalleProducto dp = carrito.buscarProducto(id);
            dp.setCantidad(dp.getCantidad()-cantidad);

            if(dp.getCantidad()<1){
                carrito.getListaProductos().remove(dp);
            }


            ctx.redirect("/carrito");
        });

        app.before("/procesar", ctx ->{
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            if(carrito.cartSize()==0){
                ctx.redirect("/carrito");
            }
            //si no esta vacio, continua con el request
        });

        app.get("/procesar",ctx -> {

            CarroCompra carrito = ctx.sessionAttribute("carrito");
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            List<DetalleProducto> dps = new ArrayList<>();

            for (DetalleProducto dp : carrito.getListaProductos()) {
                dps.add(dp);
                DetalleProductoServices.getInstancia().crear(dp);
            }

            Venta venta = new Venta(formatter.format(date),ctx.sessionAttribute("user"),dps);

            VentaServices.getInstancia().crear(venta);

            carrito.getListaProductos().clear();

            ctx.redirect("/success.html");

        });

        app.before("/ventasRealizadas",ctx ->{
           if(ctx.sessionAttribute("user")==null){
               ctx.redirect("/login.html");
           }else{
               if(!ctx.sessionAttribute("user").equals("admin")){
                   ctx.redirect("error_permiso.html");
               }
           }
        });

        app.get("/ventasRealizadas", ctx ->{
           // ArrayList<Venta> lista = controladora.getHistorialVentas();
            List<Venta> lista = VentaServices.getInstancia().findAll();
            Map<String, Object> modelo = new HashMap<>();
            
            modelo.put("lista",lista);
            modelo.put("size",((CarroCompra)ctx.sessionAttribute("carrito")).getListaProductos().size());

            ctx.render("/templates/ver_ventas.html",modelo);

            /*for (Venta v: lista ) {
                for ( DetalleProducto dp : v.getListaProductos()){
                    System.out.println("Producto id "+dp.getProducto().getId()+"Cantidad "+dp.getCantidad());
                }
            }*/
        });

        app.get("/prodInfo/:id",ctx ->{

            int id = ctx.pathParam("id",Integer.class).get();
            Map<String, Object> modelo = new HashMap<>();
            List<Comentario> lista;
            Producto producto = ProductoServices.getInstancia().find(id);
            //getting comentarioservices entity manager

            EntityManager em = ComentarioServices.getInstancia().getEntityManager();

            String queryString = "SELECT c FROM Comentario c " +
                    "WHERE c.producto.id = :id";

            Query query = em.createQuery(queryString);

            query.setParameter("id", id);

            lista = query.getResultList();

            modelo.put("lista", lista);
            modelo.put("producto", producto);
            modelo.put("cant",lista.size());
            modelo.put("usuario",ctx.sessionAttribute("user"));

            ctx.render("/templates/vistaComentario.html",modelo);

        });

        app.post("/newComentario",ctx ->{

            int id = ctx.formParam("id", Integer.class).get();
            Producto producto = ProductoServices.getInstancia().find(id);
            Usuario usuario = UsuarioServices.getInstancia().find(ctx.sessionAttribute("user"));
            String content = ctx.formParam("contenido", String.class).get();

            Comentario comentario = new Comentario(usuario,producto,content);

            ComentarioServices.getInstancia().crear(comentario);

            ctx.redirect("/prodInfo/"+id);

        });

        app.post("/eliminarComentario/:id", ctx ->{
            //GETTING ID
            int id = ctx.pathParam("id",Integer.class).get();
            int idProd = ctx.formParam("idProd", Integer.class).get();
            //REMOVING FROM DATABASE
            ComentarioServices.getInstancia().eliminar(id);
            ctx.redirect("/prodInfo/"+idProd);
        });

        }
}