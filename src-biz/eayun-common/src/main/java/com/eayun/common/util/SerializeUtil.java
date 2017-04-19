package com.eayun.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializeUtil {

    public static final String  NOT_SERIALIZED = "___NOT_SERIALIZABLE_EXCEPTION___";

    private static final Logger log            = LoggerFactory.getLogger(SerializeUtil.class);

    /**序列化*/
    public static byte[] serialize(Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    /**反序列化*/
    @SuppressWarnings("unused")
    public static Map<String, Object> unserialize(byte[] bytes) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object serializedMetadata = (Object) ois.readObject();
            Long creationTime = ((Long) ois.readObject()).longValue();
            Long lastAccessedTime = ((Long) ois.readObject()).longValue();
            Integer maxInactiveInterval = ((Integer) ois.readObject()).intValue();
            Boolean isNew = ((Boolean) ois.readObject()).booleanValue();
            Boolean isValid = ((Boolean) ois.readObject()).booleanValue();
            Long thisAccessedTime = ((Long) ois.readObject()).longValue();
            String id = (String) ois.readObject();

            int n = ((Integer) ois.readObject()).intValue();

            boolean isValidSave = isValid;
            isValid = true;
            for (int i = 0; i < n; i++) {
                String name = (String) ois.readObject();
                Object value = ois.readObject();
                if ((value instanceof String) && (value.equals(NOT_SERIALIZED)))
                    continue;
                attributes.put(name, value);
            }
            isValid = isValidSave;
            return attributes;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
