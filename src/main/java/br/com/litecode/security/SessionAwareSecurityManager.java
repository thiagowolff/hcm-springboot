package br.com.litecode.security;

//@ApplicationScope
//@Component
//public class SessionAwareSecurityManager extends DefaultWebSecurityManager {
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private UserSessionTracker userSessionTracker;
//
//    @Override
//    public Subject createSubject(SubjectContext context){
//        Subject subject = super.createSubject(context);
//
//        if (subject.isRemembered()) {
//            Session session = subject.getSession(true);
//
//            User user = userRepository.findByUsername(subject.toString());
//            user.setSessionId((String) session.getId());
//
//            userRepository.save(user);
//            userSessionTracker.addUserSession(user, subject);
//            Faces.getSessionMap().put("loggedUser", user);
//        }
//
//        return subject;
//    }
//}