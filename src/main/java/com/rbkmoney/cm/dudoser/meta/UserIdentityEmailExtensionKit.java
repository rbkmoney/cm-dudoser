package com.rbkmoney.cm.dudoser.meta;

public class UserIdentityEmailExtensionKit extends AbstractUserIdentityExtensionKit {

    public static final String KEY = "user-identity.email";

    public static final UserIdentityEmailExtensionKit INSTANCE = new UserIdentityEmailExtensionKit();

    public UserIdentityEmailExtensionKit() {
        super(KEY);
    }
}
