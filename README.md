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

![Mongo](https://www.google.com/imgres?imgurl=https%3A%2F%2Flive.mrf.io%2Fstatics%2Fi%2Fps%2Fwww.muylinux.com%2Fwp-content%2Fuploads%2F2019%2F01%2Fmongodb.png%3Fwidth%3D1200%26enable%3Dupscale&imgrefurl=https%3A%2F%2Fwww.muylinux.com%2F2019%2F01%2F17%2Fmongodb-rechazo-nueva-licencia%2F&tbnid=rChUM9JZAwooSM&vet=12ahUKEwje8ZS2lcT8AhVvUqQEHd1IDtEQMygDegUIARCoAQ..i&docid=0gSv3g23FJ9l8M&w=1200&h=801&q=Mongo&ved=2ahUKEwje8ZS2lcT8AhVvUqQEHd1IDtEQMygDegUIARCoAQ)
![SpringData](https://www.google.com/imgres?imgurl=https%3A%2F%2Fspring.io%2Fimages%2Fspring-initializr-4291cc0115eb104348717b82161a81de.svg&imgrefurl=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-data&tbnid=uYC_yEn1IiVsmM&vet=12ahUKEwilxu3ClcT8AhU5XaQEHVbMAzoQMygGegUIARDOAQ..i&docid=acf3UCpSJN6RGM&w=800&h=719&q=SpringData&ved=2ahUKEwilxu3ClcT8AhU5XaQEHVbMAzoQMygGegUIARDOAQ)
![Koin](https://www.google.com/imgres?imgurl=https%3A%2F%2Fwww.kotzilla.io%2Fwp-content%2Fuploads%2F2022%2F01%2Fkotzilla-moodboard_Koin_format-site-web-line.png&imgrefurl=https%3A%2F%2Fwww.kotzilla.io%2Fkoin%2F&tbnid=L2sJlV3frxHVMM&vet=12ahUKEwizhLjNlcT8AhUSpEwKHZ3gDtQQMygDegUIARC4AQ..i&docid=t9HCQhXyFd90vM&w=1405&h=417&q=Koin&ved=2ahUKEwizhLjNlcT8AhUSpEwKHZ3gDtQQMygDegUIARC4AQ)
![Kotlin](https://www.google.com/imgres?imgurl=https%3A%2F%2Fricardogeek.com%2Fwp-content%2Fuploads%2F2017%2F08%2Fkotlin_250x250.png&imgrefurl=https%3A%2F%2Fricardogeek.com%2Fentendiendo-las-funciones-de-alcance-en-kotlin%2F&tbnid=SZVPYcsv_1MziM&vet=12ahUKEwi9yPrelcT8AhUbVaQEHTk4DpsQMygRegUIARD_AQ..i&docid=cgXL3FVQQROsmM&w=250&h=250&q=Kotlin&ved=2ahUKEwi9yPrelcT8AhUbVaQEHTk4DpsQMygRegUIARD_AQ)
