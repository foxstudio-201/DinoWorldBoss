package org.foxstudio.dinoworldboss.blockentity;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.HashMap;
import java.util.Map;

/**
 * AnimatableInstanceCache tách riêng AnimatableManager theo instance id.
 * Block entity mặc định chỉ có 1 manager (bỏ qua id) -> 4 model dùng chung animation state,
 * nên chỉ model đầu chạy. Cache này cho mỗi id một manager riêng.
 */
public class PerIdAnimatableInstanceCache extends AnimatableInstanceCache {

    private final Map<Long, AnimatableManager<?>> managers = new HashMap<>();

    public PerIdAnimatableInstanceCache(GeoAnimatable animatable) {
        super(animatable);
    }

    @Override
    public <T extends GeoAnimatable> AnimatableManager<T> getManagerForId(long id) {
        @SuppressWarnings("unchecked")
        AnimatableManager<T> manager = (AnimatableManager<T>) managers.computeIfAbsent(id,
                k -> new AnimatableManager<>(this.animatable));
        return manager;
    }
}
