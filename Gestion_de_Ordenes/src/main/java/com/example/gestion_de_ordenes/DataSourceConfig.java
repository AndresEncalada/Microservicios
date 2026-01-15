package com.example.gestion_de_ordenes;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
@DataSourceDefinition(
    name = "java:app/jdbc/OrdenDS",  // Nombre clave de la conexión
    className = "com.mysql.cj.jdbc.MysqlDataSource",
    user = "root",
    password = "root",
    url = "jdbc:mysql://mysql-db:3306/ordenes_db?allowPublicKeyRetrieval=true&useSSL=false"
)
public class DataSourceConfig {
    // Esta clase solo sirve para configurar la conexión al iniciar
}