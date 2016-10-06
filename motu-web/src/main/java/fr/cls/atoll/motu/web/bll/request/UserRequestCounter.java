package fr.cls.atoll.motu.web.bll.request;

import java.util.HashMap;
import java.util.Map;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class UserRequestCounter {

    private Map<String, Integer> userRqtCountmap;
    public static final String ANONYMOUS_USERID = "anonymous";

    public UserRequestCounter() {
        userRqtCountmap = new HashMap<String, Integer>();
    }

    private String getUserIdWithAnonymousUserIdWhenUserIdIsNull(String userId_) {
        String uid = userId_;
        if (uid == null) {
            uid = ANONYMOUS_USERID;
        }
        return uid;
    }

    /**
     * Increment user.
     *
     * @param userId_ the key
     * @return the integer
     */
    public Integer onNewRequestForUser(String userId_) {
        String uid = getUserIdWithAnonymousUserIdWhenUserIdIsNull(userId_);

        Integer nbRqtForUser = userRqtCountmap.get(uid);
        if (nbRqtForUser == null) {
            nbRqtForUser = 0;
        }
        nbRqtForUser++;
        userRqtCountmap.put(uid, nbRqtForUser);

        return nbRqtForUser;
    }

    /**
     * Decrement user.
     *
     * @param key the key
     * @return the integer
     */
    public Integer onRequestStoppedForUser(String userId_) {
        String uid = getUserIdWithAnonymousUserIdWhenUserIdIsNull(userId_);
        Integer nbRqtForUser = userRqtCountmap.get(uid);
        if (nbRqtForUser != null) {
            nbRqtForUser--;
            if (nbRqtForUser > 0) {
                userRqtCountmap.put(uid, nbRqtForUser);
            } else {
                userRqtCountmap.remove(uid);
            }
        }

        return nbRqtForUser;
    }

    public int getRequestCount(String userId_) {
        Integer i = userRqtCountmap.get(getUserIdWithAnonymousUserIdWhenUserIdIsNull(userId_));
        return i == null ? 0 : i;
    }

}
