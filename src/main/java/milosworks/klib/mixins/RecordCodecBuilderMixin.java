package milosworks.klib.mixins;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import milosworks.klib.serialization.BuilderMapCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(value = RecordCodecBuilder.class, remap = false)
public abstract class RecordCodecBuilderMixin<O, F> {
    @Inject(method = "build", at = @At("HEAD"), cancellable = true, remap = false)
    @SuppressWarnings("unchecked")
    private static <O> void injectBuild(App<RecordCodecBuilder.Mu<O>, O> builderBox, CallbackInfoReturnable<MapCodec<O>> cir) {
        RecordCodecBuilder<O, O> builder = RecordCodecBuilder.unbox(builderBox);
        cir.setReturnValue(new BuilderMapCodec<O>() {
            @Override
            public <T> DataResult<O> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return ((RecordCodecBuilderAccessor<O, O>) (Object) builder).getDecoder().decode(ops, input);
            }

            @Override
            public <T> RecordBuilder<T> encode(final O input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return ((RecordCodecBuilderAccessor<O, O>) (Object) builder).getEncoder().apply(input).encode(input, ops, prefix);
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return ((RecordCodecBuilderAccessor<O, O>) (Object) builder).getDecoder().keys(ops);
            }

            @Override
            public String toString() {
                return "RecordCodec[" + ((RecordCodecBuilderAccessor<O, O>) (Object) builder).getDecoder() + "]";
            }

            @Override
            public RecordCodecBuilder<O, O> builder() {
                return builder;
            }
        });
    }
}
