package com.example.examplemod.network;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class ClientBedData {
    // 서버로부터 받은 침대 좌표 목록을 전역적으로 보관
    public static final List<BedInfo> PLAYER_BEDS = new ArrayList<>();

    public static class BedInfo {
        public final BlockPos pos;
        public final boolean isActive;

        public BedInfo(BlockPos pos, boolean isActive) {
            this.pos = pos;
            this.isActive = isActive;
        }
    }

    // [추가] 서버에서 온 문자열 데이터를 파싱하여 리스트를 갱신하는 메서드
    public static void handleBedData(String data) {
        PLAYER_BEDS.clear();

        // 데이터가 "none"이거나 비어있으면 종료
        if (data == null || data.isEmpty() || data.equals("active:none")) return;

        String[] parts = data.split("\\|");
        String activeBedStr = "";

        // 1. 활성화된 침대가 무엇인지 먼저 확인
        for (String part : parts) {
            if (part.startsWith("active:")) {
                activeBedStr = part.replace("active:", "");
                break;
            }
        }

        // 2. 침대 좌표들 생성
        for (String part : parts) {
            if (part.startsWith("active:")) continue;

            BlockPos pos = parseLoc(part);
            if (pos != null) {
                // 현재 좌표 문자열이 활성화된 침대 문자열과 같으면 isActive = true
                boolean isActive = part.equals(activeBedStr);
                PLAYER_BEDS.add(new BedInfo(pos, isActive));
            }
        }
    }

    private static BlockPos parseLoc(String s) {
        try {
            String[] p = s.split(",");
            // p[0]=world, p[1]=x, p[2]=y, p[3]=z
            return new BlockPos(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
        } catch (Exception e) {
            return null;
        }
    }
}