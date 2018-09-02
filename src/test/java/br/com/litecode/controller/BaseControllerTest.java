package br.com.litecode.controller;

import br.com.litecode.domain.model.User;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.omnifaces.component.output.cache.CacheFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@DataJpaTest
@ComponentScan(basePackages = "br.com.litecode")
@PrepareForTest({ FacesContext.class, CacheFactory.class })
@WithUserDetails("admin")
public abstract class BaseControllerTest {

    @Mock
    protected FacesContext facesContext;

    @Mock
    private ExternalContext externalContext;

    @Before
    public void setUpMocks() {
        User loggedUser = new User();
        loggedUser.setUserId(1);
        loggedUser.setUsername("admin");

        PowerMockito.mockStatic(FacesContext.class);
        when(FacesContext.getCurrentInstance()).thenReturn(facesContext);
        when(facesContext.getExternalContext()).thenReturn(externalContext);

        PowerMockito.mockStatic(CacheFactory.class);
        when(CacheFactory.getCache(facesContext, "session")).thenReturn(null);
    }
}
