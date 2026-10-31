create database if not exists pediacenter_app character set utf8mb4 collate utf8mb4_unicode_ci;
use pediacenter_app;
create table if not exists usuarios (id int auto_increment primary key, usuario varchar(60) not null unique, clave_hash varchar(180) not null, rol varchar(20) not null);
create table if not exists pacientes (id int auto_increment primary key, nombre varchar(160) not null, fecha_nacimiento date not null, sexo varchar(20) not null, responsable varchar(160), telefono varchar(40), direccion varchar(250), creado timestamp default current_timestamp);
create table if not exists citas (id int auto_increment primary key, paciente_id int not null, fecha datetime not null, motivo varchar(250) not null, estado varchar(30) not null default 'PROGRAMADA', foreign key (paciente_id) references pacientes(id), index(fecha));
create table if not exists consultas (id int auto_increment primary key, paciente_id int not null, fecha datetime not null, motivo varchar(250) not null, diagnostico text, tratamiento text, observaciones text, foreign key (paciente_id) references pacientes(id));
create table if not exists crecimiento (id int auto_increment primary key, paciente_id int not null, fecha date not null, peso_kg decimal(7,2) not null, talla_cm decimal(7,2) not null, observaciones text, foreign key (paciente_id) references pacientes(id));
create table if not exists examenes (id int auto_increment primary key, paciente_id int not null, fecha date not null, nombre varchar(160) not null, resultado text, observaciones text, foreign key (paciente_id) references pacientes(id));

create table if not exists archivos (id int auto_increment primary key, examen_id int not null unique, nombre varchar(255) not null, contenido mediumblob not null, foreign key (examen_id) references examenes(id) on delete cascade);
