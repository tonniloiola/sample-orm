package com.example.demo;

import com.example.demo.entity.CalculationTablePis;

public class DemoApplication {

	public static void main(String[] args) {
	
		CalculationTablePis calc = new CalculationTablePis();
		
		calc.findAll();
		
		calc.findByPrimaryKey(1);
	
	}
}
