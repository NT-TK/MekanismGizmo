package jp.example.mekanismgizmo.block;

import jp.example.mekanismgizmo.util.MachineTier;

/** Implemented by the block of every tiered machine, so the block entity and the tier installer can read its tier. */
public interface TieredMachine {

    MachineTier getTier();
}
