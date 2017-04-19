package com.eayun.virtualization.model;

public class TagGroup extends BaseTagGroup {

    private static final long serialVersionUID = 2824474020105173668L;

    private String            tgrpEnabled;                            //是否启用
    private String            tgrpUnique;                             //是否唯一
    private long              tgrpTagNum;                             //标签数量
    private long              tgrpResNum;                             //资源数量
    private String            tgrpCreator;                            //创建人名称
    private String            tgrpCreateDate;

    public String getTgrpEnabled() {
        return tgrpEnabled;
    }

    public void setTgrpEnabled(String tgrpEnabled) {
        this.tgrpEnabled = tgrpEnabled;
    }

    public String getTgrpUnique() {
        return tgrpUnique;
    }

    public void setTgrpUnique(String tgrpUnique) {
        this.tgrpUnique = tgrpUnique;
    }

    public long getTgrpTagNum() {
        return tgrpTagNum;
    }

    public void setTgrpTagNum(long tgrpTagNum) {
        this.tgrpTagNum = tgrpTagNum;
    }

    public long getTgrpResNum() {
        return tgrpResNum;
    }

    public void setTgrpResNum(long tgrpResNum) {
        this.tgrpResNum = tgrpResNum;
    }

    public String getTgrpCreator() {
        return tgrpCreator;
    }

    public void setTgrpCreator(String tgrpCreator) {
        this.tgrpCreator = tgrpCreator;
    }

    public String getTgrpCreateDate() {
        return tgrpCreateDate;
    }

    public void setTgrpCreateDate(String tgrpCreateDate) {
        this.tgrpCreateDate = tgrpCreateDate;
    }
}
