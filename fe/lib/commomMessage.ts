import { ApiErrorResponse } from "@/types";
import axios from "axios";

export function getErrorMessage(error: unknown, fallback: string): string {
    if (axios.isAxiosError<ApiErrorResponse>(error)) {
        return error.response?.data?.message ?? fallback;
    }

    return fallback;
}
