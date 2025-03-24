package domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 */
public class Prietenie extends Entity<Long> {
    private Long nodPrietenie1;
    private Long nodPrietenie2;
    private LocalDateTime friendsFrom;
    private String status;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private boolean notificareTrimisa = false;

    public boolean isNotificareTrimisa() {
        return notificareTrimisa;
    }

    public void setNotificareTrimisa(boolean notificareTrimisa) {
        this.notificareTrimisa = notificareTrimisa;
    }


    /**
     *
     * @param nodPrietenie1
     * @param nodPrietenie2
     */
    public Prietenie(Long nodPrietenie1, Long nodPrietenie2, LocalDateTime friendsFrom) {
        this.nodPrietenie1 = nodPrietenie1;
        this.nodPrietenie2 = nodPrietenie2;
        this.friendsFrom = friendsFrom;
        this.status = "PENDING";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void acceptFriend() {
        this.status = "ACCEPTED";
        this.friendsFrom=LocalDateTime.now();
    }

    public Long getNodPrietenie1() {
        return nodPrietenie1;
    }

    public void setNodPrietenie1(Long nodPrietenie1) {
        this.nodPrietenie1 = nodPrietenie1;
    }

    public Long getNodPrietenie2() {
        return nodPrietenie2;
    }

    public void setNodPrietenie2(Long nodPrietenie2) {
        this.nodPrietenie2 = nodPrietenie2;
    }

    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    @Override
    public String toString() {
        return "Prietenie{" +
                "nodPrietenie1=" + nodPrietenie1 +
                ", nodPrietenie2=" + nodPrietenie2 +
                ", friendsFrom=" + friendsFrom.format(formatter) +
                '}';
    }
}
