package zly.rivulet.spring.base.proxy;

import org.springframework.dao.support.DaoSupport;
import zly.rivulet.base.Rivulet;
import zly.rivulet.base.RivuletManager;

public abstract class RivuletDaoSupport extends DaoSupport {

    private Rivulet rivuletTemplate;

    public void setRivuletTemplate(Rivulet rivuletTemplate) {
        this.rivuletTemplate = rivuletTemplate;
    }

    public Rivulet getRivuletTemplate() {
        return rivuletTemplate;
    }


    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
        if (this.rivuletTemplate == null) {
            throw new IllegalArgumentException("必须存在rivuletTemplate");
        }

    }

    @Override
    protected void initDao() throws Exception {
        super.initDao();
        RivuletManager rivuletManager = rivuletTemplate.getRivuletManager();
//        rivuletManager.putInStorage();

    }
}
