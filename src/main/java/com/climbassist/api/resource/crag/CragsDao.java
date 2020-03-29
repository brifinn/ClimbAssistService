package com.climbassist.api.resource.crag;

import com.climbassist.api.resource.common.ResourceDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CragsDao extends ResourceDao<Crag> {

    @Override
    protected Crag buildResourceForDeletion(@NonNull String resourceId) {
        return Crag.builder()
                .cragId(resourceId)
                .build();
    }

    @Override
    protected Crag buildIndexHashKey(@NonNull String parentId) {
        return Crag.builder()
                .subAreaId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return Crag.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<Crag> getResourceTypeClass() {
        return Crag.class;
    }
}
