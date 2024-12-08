package milosworks.klib.mixins;

import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(value = RecordCodecBuilder.class, remap = false)
public interface RecordCodecBuilderAccessor<O, F> {
    @Accessor(remap = false)
    Function<O, MapEncoder<F>> getEncoder();

    @Accessor(remap = false)
    MapDecoder<F> getDecoder();
}
