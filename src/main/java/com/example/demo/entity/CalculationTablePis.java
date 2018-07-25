package com.example.demo.entity;

import java.util.Date;

import com.example.demo.config.Column;
import com.example.demo.config.Entity;
import com.example.demo.config.Relationship;
import com.example.demo.config.SimpleDaoImpl;

@Entity(name="tabela_pis")
public class CalculationTablePis extends SimpleDaoImpl<CalculationTablePis> {
	
	@Column(name="id_tabela_pis", primary=true)
	private Integer id;
	
	@Column(name="vigencia_inicial")
	private Date initialTerm;
	
	@Column(name="vigencia_final")
	private Date finalTerm;
	
	@Column(name="aliquota")
	private Double aliquot;
	
//	@Column(name="id_usuario_criacao")
//	@OneToOne
////	@JoinColumn(name="id_usuario_criacao")
//	@JoinTable(name="pessoa", 
//				joinColumns = @JoinColumn(name="pessoa_id"), 
//				inverseJoinColumns = @JoinColumn(name="id_usuario_criacao"))
	
	// One To One relationship: the Address is stored somewhere else and we have
    // the member "addressId" which we have to refer to on the Address class 
	@Relationship(single=true, member="id")
	private User creationUser;
	
	@Column(name="data_criacao")
	private Date creationDate;
	
	@Column(name="id_usuario_alteracao")
	private Integer updateUser;
	
	@Column(name="data_alteracao")
	private Date updateDate;
	
	@Column(name="id_usuario_exclusao")
	private Integer exclusionUser;

	@Column(name="data_exclusao")
	private Date exclusionDate;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getInitialTerm() {
		return initialTerm;
	}

	public void setInitialTerm(Date InitialTerm) {
		this.initialTerm = InitialTerm;
	}

	public Date getFinalTerm() {
		return finalTerm;
	}

	public void setFinalTerm(Date finalTerm) {
		this.finalTerm = finalTerm;
	}

	public Double getAliquot() {
		return aliquot;
	}

	public void setAliquot(Double aliquot) {
		this.aliquot = aliquot;
	}

	public User getCreationUser() {
		return creationUser;
	}

	public void setCreationUser(User creationUser) {
		this.creationUser = creationUser;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Integer getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(Integer updateUser) {
		this.updateUser = updateUser;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	
	public Integer getExclusionUser() {
		return exclusionUser;
	}

	public void setExclusionUser(Integer exclusionUser) {
		this.exclusionUser = exclusionUser;
	}

	public Date getExclusionDate() {
		return exclusionDate;
	}

	public void setExclusionDate(Date exclusionDate) {
		this.exclusionDate = exclusionDate;
	}
	
	@Override
	public String toString() {
		return "id: " + id + "\n" + 
				"initialTerm: " + initialTerm + "\n"+
				"finalTerm: " + finalTerm + "\n" +
				"aliquot: " + aliquot + "\n" +
				"creationUser: " + creationUser + "\n\n";
	}
	
	public CalculationTablePis persist() {
		return (CalculationTablePis) super.persist(this);
	}
	
	public void delete() {
		super.delete(this);
	}
	
}
