# AD-P03-TennislabNoSQL

## ¿Qué es TennislabNoSQL?

Se trata de una aplicación centrada en bases de datos no relacionales, en este caso hacemos uso de [mongo](https://www.mongodb.com/es) en ambos casos, en la primera aplicación trabajamos con Mongo a pelo, mientras que en la segunda aplicación hemos convertido la aplicación para trabajar con [Spring Data](https://spring.io/projects/spring-data).

## Planteamiento

![diagrama](https://github.com/IvanAzagraTroya/AD-P03-TennislabNoSQL/blob/baef18306d766606908d8fe6da440e4b69de49a6/TennisLab-Mongo/diagram/diagrama.png)

Tenemos varios modelos con los que trabajar de los que hablaré a continuación:

- **Pedido**: Este objeto está compuesto con un identificador, los estados que puede tener el pedido y las fechas requeridas, estas son la fecha de entrada del pedido, la fecha programada de salida, la fecha real de salida, y la fecha tope de entrega, este planteamiento viene dado para tener un registro de cuándo se recibe el pedido, cuándo es la estimación en la que se piensa que se entregará para dar una fecha al cliente cuando se recibe el pedido, esta fecha después se puede actualizar a la fecha de salida real, que podría no ser la misma y por último tendremos la fecha de entrega, que podría variar en unos pocos días, ya que cuando sale el pedido de las instalaciones no tiene por qué ser el mismo día que el pedido llegue al cliente, o que el mismo lo recoja.

- **Tarea**:

- **Producto**: Este objeto está compuesto de un identificador, el **tipo de producto** que es, ya que podrían ser **raquetas, cordajes, overgrips, grips, antivibradores y fundas**,
  el modelo también tiene registros de las marcas y modelos los cuáles son cadenas de texto, el precio del producto que se trata con un Double y el stock actual.

- **User**: Este objeto será la representación de los usuarios del sistema, se compone de un identificador, el nombre, apellido, teléfono, email, la contraseña que será encriptada con sha512, el tipo de perfil que podrá ser **admin, worker o client** y por último un valor de si está activo o no para conservar el registro pero sin poder acceder a la cuenta en caso de estar inactiva. Guardamos los datos pero no se permite más el acceso si la cuenta está inactiva.

- **Turno**: Este objeto representa la jornada con un identificador en la que se ha completado una o dos tareas, las cuales son referenciadas a través de su identificador, este modelo se compone por la referencia al identificador del trabajador, y al de la máquina, las horas de inicio y de fin que son dos **LocalDateTime** y el número de pedidos activos, solo es obligatoria una referencia a una tarea, ya que es necesario que mínimo haya una tarea para guardar el registro del turno, por ello la segunda es de tipo **String?** con lo que admitimos valores nullables en este.

- **Máquina**: Se trata de la representación de la máquina que se haya utilizado para trabajar durante el turno, está compuesto por un identificador, modelo, marca y número de serie de tipo **String**, la fecha de adquisición de la máquina **LocalDate**, el tipo de la máquina que podrá ser **encordadora o personalizadora** y por último se encapsulan los datos en el valor data representado el cuál será los atributos de específicos del tipo de máquina del que se trata.

### En cuanto a las relaciones:

- **Pedido-User**: Se trata de una relación _0..N-1_ ya que el usuario puede darse de alta en la plataforma pero no realizar ningún pedido, o por otro lado podría realizar múltiples, sin embargo para que haya un pedido debe de existir un usuario que lo realice.

- **Pedido-Tarea**: Se trata de una relación _1-1..N_ ya que cada pedido está compuesto por mínimo una tarea y puede tener tantas tareas como requisitos establezca el cliente.

- **User-Turno**: Se trata de una relación _1-0..N_ ya que tiene que haber un usuario de tipo _worker_ para poder realizar el turno de trabajo, sin un trabajador disponible no puede haber un turno y por ello no hay una máquina en uso ni una o varias tareas que se vayan a cumplir durante ese turno.

- **Tarea-Producto**: Se trata de una relación 0..N-1 ya que el producto existirá aunque no haya una tarea existente en ese momento, sin embargo también puede haber varias tareas que requieran de los productos.

- **Tarea-Turno**: Se trata de una relación 1..N-1 ya que puede haber entre 1 y varias tareas durante el transcurso de un turno y por la existencia de las tareas habrá un turno en las que se irán cumpliendo.

- **Turno-Maquina**: Se trata de una relación 0..N-1 ya que la máquina siempre va a existir aunque no se esté realizando ningún turno en ese momento, mientras que puede haber varios turnos que hagan uso de máquinas.

## ¿Cómo funciona?

De momento printea el código plantilla de intellij.

## Tencnologías:

![Mongo](https://user-images.githubusercontent.com/67174666/212279453-f2a9887b-29d3-4394-a753-b0ef7d3a428a.png)

![SpringData](https://user-images.githubusercontent.com/67174666/212279518-eca7216b-ab3b-409b-87a7-09ac95461dab.png)

![Koin](https://user-images.githubusercontent.com/67174666/212279657-d90c0aa4-8741-456c-9f70-e02887f204db.png)

![Kotlin](https://user-images.githubusercontent.com/67174666/212279750-122c6f68-7b30-4ba8-a003-1db50df5feec.png)

