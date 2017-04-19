package com.eayun.cdn.factory;

import com.eayun.cdn.intf.CDN;

/**
 * CDN抽象工厂
 */
public abstract class AbstractCDNFactory {
    public abstract <T extends CDN> T createCDN(Class<T> c);
}
