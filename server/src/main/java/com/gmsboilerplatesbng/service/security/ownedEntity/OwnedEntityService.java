package com.gmsboilerplatesbng.service.security.ownedEntity;

import com.gmsboilerplatesbng.domain.security.ownedEntity.EOwnedEntity;
import com.gmsboilerplatesbng.repository.security.ownedEntity.EOwnedEntityRepository;
import com.gmsboilerplatesbng.util.constant.DefaultConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class OwnedEntityService {

    private final DefaultConst c;

    private final EOwnedEntityRepository entityRepository;


    @Autowired
    public OwnedEntityService(EOwnedEntityRepository entityRepository, DefaultConst defaultConst) {
        this.entityRepository = entityRepository;
        this.c = defaultConst;
    }

    //region default entity
    public EOwnedEntity createDefaultEntity() {
        return this.entityRepository.save(
                new EOwnedEntity(this.c.ENTITY_NAME, this.c.ENTITY_USERNAME, this.c.ENTITY_DESCRIPTION)
        );
    }
    //endregion

}