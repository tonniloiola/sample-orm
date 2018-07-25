package com.example.demo.entity;

import com.example.demo.config.Column;
import com.example.demo.config.Entity;

@Entity(name="pessoa")
public class User  {

	@Column(name="id_pessoa")
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
