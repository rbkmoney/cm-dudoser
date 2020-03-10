package com.rbkmoney.cm.dudoser.meta;

import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataConversionException;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataConverter;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtension;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtensionKitImpl;

public class UserIdentityRealmExtensionKit extends MetadataExtensionKitImpl<UserTypeEnum> {

    public static final String KEY = "user-identity.realm";

    public static final UserIdentityRealmExtensionKit INSTANCE = new UserIdentityRealmExtensionKit();

    public UserIdentityRealmExtensionKit() {
        super(
                new MetadataExtension<>() {

                    @Override
                    public UserTypeEnum getValue(Metadata metadata) {
                        return metadata.getValue(KEY);
                    }

                    @Override
                    public void setValue(UserTypeEnum val, Metadata metadata) {
                        metadata.putValue(KEY, val);
                    }
                },
                new MetadataConverter<>() {

                    @Override
                    public UserTypeEnum convertToObject(String key, String value) throws MetadataConversionException {
                        return UserTypeEnum.valueOf(value);
                    }

                    @Override
                    public String convertToString(String key, UserTypeEnum value) throws MetadataConversionException {
                        return value.toString();
                    }

                    @Override
                    public boolean apply(String key) {
                        return key.equalsIgnoreCase(KEY);
                    }
                }
        );
    }
}
