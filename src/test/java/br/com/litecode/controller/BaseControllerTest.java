package br.com.litecode.controller;

import br.com.litecode.TestConfig;
import de.larmic.joinfaces.test.FacesContextMockApplicationContextInitializer;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@WithUserDetails("admin")
@Import(TestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {FacesContextMockApplicationContextInitializer.class})
public abstract class BaseControllerTest {
    @Autowired
    protected ApplicationContext applicationContext;
}
