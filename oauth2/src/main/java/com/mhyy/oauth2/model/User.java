package com.mhyy.oauth2.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_name")
    private String username;

    @Column(name = "passwd")
    private String password;

    @Column(name = "user_role")
    private String userRole;
}
