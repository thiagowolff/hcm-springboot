package br.com.litecode.controller;

import br.com.litecode.domain.model.User;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@DataJpaTest
@ComponentScan(basePackages = "br.com.litecode")
@PrepareForTest({ FacesContext.class })
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

        SecurityManager securityManger = mock(SecurityManager.class);
        Subject subject = mock(Subject.class);

        when(securityManger.createSubject(any(SubjectContext.class))).thenReturn(subject);
        when(subject.getPrincipal()).thenReturn(loggedUser.getUsername());

        ThreadContext.bind(securityManger);

        PowerMockito.mockStatic(FacesContext.class);
        when(FacesContext.getCurrentInstance()).thenReturn(facesContext);
        when(facesContext.getExternalContext()).thenReturn(externalContext);

        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("loggedUser", loggedUser);

        when(facesContext.getExternalContext().getSessionMap()).thenReturn(sessionMap);
    }
}
