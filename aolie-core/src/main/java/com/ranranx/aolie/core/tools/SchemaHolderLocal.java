package com.ranranx.aolie.core.tools;

import com.ranranx.aolie.core.datameta.datamodel.SchemaHolder;
import com.ranranx.aolie.core.interfaces.ISchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/26 0026 22:21
 **/
@Component
public class SchemaHolderLocal extends SchemaHolder {
    @Override
    @Autowired
    public void setSchemaService(ISchemaService schemaService) {
        super.setSchemaService(schemaService);
    }
}
