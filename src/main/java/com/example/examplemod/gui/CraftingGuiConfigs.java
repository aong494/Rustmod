package com.example.examplemod.gui;

public class CraftingGuiConfigs {
    // 0~5번: 상단 카테고리 탭
    public static final int TAB_X = 185;// 왼쪽 벽에서 떨어진 거리
    public static final int TAB_Y = 107;// 위쪽 벽에서 떨어진 거리
    public static final int TAB_SPACING_Y = 20; // 탭 사이의 세로 간격 (슬롯 크기 고려)

    // 6~8번: 수량 조절 (-, 숫자, +)
    public static final int QTY_X = 351;
    public static final int QTY_Y = 274;

    // 9~44번: 제작 레시피 목록
    public static final int LIST_X = 267;
    public static final int LIST_Y = 95;

    // 45~51번: 제작 대기열 (하단 7칸)
    public static final int QUEUE_X = 348;
    public static final int QUEUE_Y = 238;

    // 53번: 제작 시작 버튼 (크게 배치할 위치)
    public static final int CRAFT_BTN_X = 474;
    public static final int CRAFT_BTN_Y = 271;
}