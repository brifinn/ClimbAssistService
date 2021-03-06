package com.climbassist.api.resource.common.batch;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;

public interface BatchCreateResourcesResult<Resource extends OrderableResourceWithParent<Resource, ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>> {

}
