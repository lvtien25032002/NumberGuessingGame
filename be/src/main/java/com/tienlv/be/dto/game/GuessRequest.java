package com.tienlv.be.dto.game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GuessRequest(
        @NotNull @Min(1) @Max(5) Integer guess
) {
}
