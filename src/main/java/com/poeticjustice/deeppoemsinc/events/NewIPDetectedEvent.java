package com.poeticjustice.deeppoemsinc.events;

import java.util.Objects;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter 
@Getter
public class NewIPDetectedEvent {
    private String userId;
    private String ip;

    @Override
    public String toString() {
        return "NewIPDetectedEvent{" +
                "userId='" + userId + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewIPDetectedEvent)) return false;
        NewIPDetectedEvent that = (NewIPDetectedEvent) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, ip);
    }
}