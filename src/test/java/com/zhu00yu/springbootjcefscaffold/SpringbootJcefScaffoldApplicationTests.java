package com.zhu00yu.springbootjcefscaffold;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootJcefScaffoldApplicationTests {

	@Test
	void contextLoads() {
	}

	@Value("${jasypt.encryptor.password}")
	private String password;
	@Value("${jasypt.encryptor.algorithm}")
	private String algorithm;

	@Test
	public void testEncrypt() throws Exception {
		StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
		EnvironmentPBEConfig config = new EnvironmentPBEConfig();

		config.setAlgorithm(algorithm);          // 加密的算法，这个算法是默认的
		config.setPassword(password);                        // 加密的密钥，随便自己填写，很重要千万不要告诉别人
		standardPBEStringEncryptor.setConfig(config);
		String plainText = "123456";         //自己的密码
		String encryptedText = standardPBEStringEncryptor.encrypt(plainText);
		System.out.println("密码：" + encryptedText);
	}

	@Test
	public void testDe() throws Exception {
		StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
		EnvironmentPBEConfig config = new EnvironmentPBEConfig();

		config.setAlgorithm(algorithm);
		config.setPassword(password);
		standardPBEStringEncryptor.setConfig(config);
		String encryptedText = "DJVB6yghFjHz17/pJvu3zA==";   //加密后的密码
		String plainText = standardPBEStringEncryptor.decrypt(encryptedText);
		System.out.println(plainText);
	}


}
