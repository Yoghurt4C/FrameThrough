package mods.framethrough.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFramePassthroughMixin extends AbstractDecorationEntity {

    protected ItemFramePassthroughMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interact",at=@At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void interactThroughTheFrame(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> ctx, ItemStack heldStack){
        ItemFrameEntity frame = (ItemFrameEntity)(Object)this;
        if (!world.isClient() && !frame.getHeldItemStack().isEmpty() && !player.isSneaking() && player.canModifyWorld()) {
            ctx.setReturnValue(true);
            BlockPos anchorPos = frame.getBlockPos().offset(frame.getHorizontalFacing().getOpposite());
            BlockHitResult brtr = new BlockHitResult(player.getCameraPosVec(1F), this.getHorizontalFacing(), anchorPos, false);
            Item heldItem = heldStack.getItem();
            ItemUsageContext itemCtx = new ItemUsageContext(player, hand, brtr);
            if (!heldStack.isEmpty() && !(heldItem instanceof BlockItem) && heldItem.useOnBlock(itemCtx).isAccepted()) {
                heldStack.useOnBlock(itemCtx);
            } else { world.getBlockState(anchorPos).onUse(world, player, hand, brtr); }
        }
    }
}
