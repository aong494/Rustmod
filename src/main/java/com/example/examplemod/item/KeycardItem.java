package com.example.examplemod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class KeycardItem extends Item {
    public KeycardItem(Properties properties) {
        super(properties);
    }

    // 아이템이 인챈트 테이블 등에서 수리되지 않게 하거나
    // 추가적인 로직(예: 사용 시 소리)을 넣고 싶을 때 확장 가능합니다.
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; // 키카드는 마법 부여 불가
    }
}