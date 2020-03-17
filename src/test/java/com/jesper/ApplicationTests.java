package com.jesper;

//import com.mongodb.gridfs.GridFSDBFile;
import com.jesper.hftc.entity.SalesOrderReturnChild;
import com.jesper.mapper.SalesOrderReturnChildMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Autowired
	private SalesOrderReturnChildMapper salesOrderReturnChildMapper;
	@Test
	public void contextLoads() {
		SalesOrderReturnChild byProductId =new SalesOrderReturnChild();
		byProductId = salesOrderReturnChildMapper.getByProductId(1);
		System.out.println(byProductId==null);
		byProductId.setProductId(1);
		System.out.println(byProductId.getProductId());
	}

	@Autowired
//	private static MongoTemplate mongoTemplate;

	@Test
	public void saveFileTest() throws  Exception{
//		FileInputStream file = new FileInputStream("C:\\Users\\jiangyunxiong\\Desktop\\qq.jpg");

	}





}
