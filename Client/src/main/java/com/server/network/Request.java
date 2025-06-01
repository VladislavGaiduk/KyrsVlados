package com.server.network;

import com.server.enums.Operation;
import lombok.*;


import java.io.Serializable;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Request implements Serializable {
    @NonNull
    private Operation operation;
    private String data; // JSON-строка
}
