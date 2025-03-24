package service;

import domain.FilterUserDTO;
import domain.Message;
import domain.Prietenie;
import domain.Utilizator;
import domain.validators.ValidationException;
import events.ChangeEventType;
import events.PrietenieEntityChangeEvent;
import observer.Observable;
import observer.Observer;
import paginare.Page;
import paginare.Pageable;
import repository.PrieteniRepository;
import repository.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServiceApp implements Observable<PrietenieEntityChangeEvent> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    protected final Repository<Long, Utilizator> repoUsers;
    protected final PrieteniRepository repoPrietenie;
    protected final Repository<Long, Message> repoMessages;
    private List<Observer> observers=new ArrayList<>();

    public ServiceApp(Repository<Long, Utilizator> repoUsers, PrieteniRepository repoPrietenie, Repository<Long, Message> repoMessages) {
        this.repoUsers = repoUsers;
        this.repoPrietenie = repoPrietenie;
        this.repoMessages = repoMessages;
        loadPrietenii();
    }


    public void loadPrietenii() {
        Iterable<Prietenie> it = repoPrietenie.findAll();
        it.forEach(p->{
            Optional<Utilizator> u1 = repoUsers.findOne(p.getNodPrietenie1());
            Optional<Utilizator> u2 = repoUsers.findOne(p.getNodPrietenie2());
            if(u1.isPresent() && u2.isPresent()) {
                Utilizator user1 = u1.get();
                Utilizator user2 = u2.get();
                user1.addFriends(user2.getId());
                user2.addFriends(user1.getId());
            }
        });
    }

    public Utilizator findUtilizatoriById(Long Id) {
        Iterable<Utilizator> it = repoUsers.findAll();
        AtomicReference<Utilizator> found = new AtomicReference<>();
        it.forEach(u -> {
            if(u.getId().equals(Id))
                found.set(u);
        });
        //System.out.println(found.get().getId());
        return found.get();
    }


    public boolean adaugaUtilizator(String nume, String prenume, String username, String password) {
        Utilizator utilizator = new Utilizator(nume, prenume, username, password, "https://www.google.com/imgres?q=poza%20insta%20profil%20unknown&imgurl=https%3A%2F%2Fi.pinimg.com%2F236x%2Fd4%2F37%2F4b%2Fd4374b6dc2934880eaa7a5e8989c1f64.jpg&imgrefurl=https%3A%2F%2Fph.pinterest.com%2Fkurtdarwin47%2Funknown-picture-profile%2F&docid=VZKU5iNQj0VsYM&tbnid=GhTod9wuKqwF4M&vet=12ahUKEwjUiqaB1fyKAxV10wIHHR3KNkQQM3oECGwQAA..i&w=236&h=253&hcb=2&ved=2ahUKEwjUiqaB1fyKAxV10wIHHR3KNkQQM3oECGwQAA");
        if (findUtilizatoriByUserName(utilizator.getUserName()) != null) {
            throw new IllegalArgumentException("Username already exists!");
        }
        Optional<Utilizator> u = repoUsers.save(utilizator);
        if (u.isPresent()) {
            System.err.println("Salvarea a eșuat pentru utilizatorul: " + u.get().toString());
            throw new IllegalArgumentException("An user with this ID already exists!");
        }
        return true;
    }

    public Optional<Utilizator> removerUtilizator(Long id) {
        if(id == null)
            throw new IllegalArgumentException("Id must be not null!");
        Utilizator u = findUtilizatoriById(id);
        if (u == null)
            throw new IllegalArgumentException("Id doesn't exist!");

        try{
            deleteFriendship(id);
        }
        catch(Exception e){}
        return repoUsers.delete(u.getId());
    }


    public Iterable<Utilizator> getUtilizatori() {
        return repoUsers.findAll();
    }

    public Iterable<Prietenie> getPrietenii() {
        return repoPrietenie.findAll();
    }

    public boolean adaugaPrietenie(Long u1Id, Long u2Id) {

        if(u1Id == null || u2Id == null){
            throw new IllegalArgumentException("Ids must be not null!");
        }
        Utilizator u1 = findUtilizatoriById(u1Id);
        Utilizator u2 = findUtilizatoriById(u2Id);

        if(u1 == null || u2 == null || u1.equals(u2)){
            throw new IllegalArgumentException("Users must be not null and different!");
        }

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        Prietenie prietenie = new Prietenie(u1Id, u2Id, now);

        Optional<Prietenie> p = existPendingFriendship(prietenie);
        if(p.isPresent())
        {
            if(!Objects.equals(p.get().getNodPrietenie2(), u2Id)) {
                try {
                    Optional<Prietenie> p2 = repoPrietenie.update(p.get());
                    p2.get().setNotificareTrimisa(true);
                    notifyObservers(new PrietenieEntityChangeEvent(ChangeEventType.UPDATE));
                } catch (ValidationException e) {
                    return false;
                }
            }
        }
        else {
            try {
                Optional<Prietenie> friedshiip = repoPrietenie.save(prietenie);
                notifyObservers(new PrietenieEntityChangeEvent(ChangeEventType.ADD));
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public Optional<Prietenie> existPendingFriendship(Prietenie f1) {
        return StreamSupport.stream(getPrietenii().spliterator(), false)
                .filter(friendship ->
                        (friendship.getNodPrietenie1().equals(f1.getNodPrietenie2()) &&
                                friendship.getNodPrietenie2().equals(f1.getNodPrietenie1())) ||
                                (friendship.getNodPrietenie1().equals(f1.getNodPrietenie1()) &&
                                        friendship.getNodPrietenie2().equals(f1.getNodPrietenie2())))
                .findFirst();
    }


    public void removePrietenie(Long idU1, Long idU2) {
        Utilizator u1, u2;

        if(idU1 == null || idU2 == null)
            throw new IllegalArgumentException("Ids must not be null!");
        u1 = findUtilizatoriById(idU1);
        u2 = findUtilizatoriById(idU2);
        System.out.println("aici");
        if(u1 == null || u2 == null || u1.equals(u2))
            throw new ValidationException("Users must be not null and different!");

        Iterable<Prietenie> l = repoPrietenie.findAll();
        AtomicReference<Prietenie> found = new AtomicReference<>();
        l.forEach(el -> {
            if(
                    (el.getNodPrietenie1().equals(u1.getId()) && el.getNodPrietenie2().equals(u2.getId()))
                            || (el.getNodPrietenie1().equals(u2.getId()) && el.getNodPrietenie2().equals(u1.getId()))
            ) {
                found.set(el);
            }
        });
        if(found.get() == null){
            throw new IllegalArgumentException("These users are not friends!");
        }
        System.out.println(found.get().getId());
        Optional<Prietenie> f = repoPrietenie.delete(found.get().getId());
        if(f.isPresent()){
            notifyObservers(new PrietenieEntityChangeEvent(ChangeEventType.DELETE));
        }
    }

    public void deleteFriendship(Long id) {
        Utilizator u1;

        if (id == null)
            throw new IllegalArgumentException("Id must not be null!");
        u1 = findUtilizatoriById(id);

        for (Long prieteniId : u1.getFriends()) {
            Utilizator user = findUtilizatoriById(prieteniId);
            user.removeFriends(id);
        }

        Iterable<Prietenie> l = repoPrietenie.findAll();
        l.forEach(el -> {
            if (el.getNodPrietenie1().equals(id) || el.getNodPrietenie2().equals(id))
                repoPrietenie.delete(el.getId());
        });

        throw new IllegalArgumentException("These users are not friends!");
    }

    public Utilizator findUtilizatoriByUserName(String userName) {
        Iterable<Utilizator> it = repoUsers.findAll();
        AtomicReference<Utilizator> found = new AtomicReference<>();
        it.forEach(u -> {
            if(u.getUserName().equals(userName))
                found.set(u);
        });
        return found.get();
    }

    public Iterable<Utilizator> getPrieteniUser(Utilizator user) {
        List<Utilizator> friends = new ArrayList<>();

        repoPrietenie.findAll().forEach(prietenie -> {
            if (prietenie.getNodPrietenie1().equals(user.getId())) {
                repoUsers.findOne(prietenie.getNodPrietenie2()).ifPresent(friends::add);
            } else if (prietenie.getNodPrietenie2().equals(user.getId())) {
                repoUsers.findOne(prietenie.getNodPrietenie1()).ifPresent(friends::add);
            }
        });
        System.out.println(friends);
        return friends.stream().distinct().toList();
    }

    public Iterable<Prietenie> getFriendsRequest(Utilizator user) {
        return StreamSupport.stream(repoPrietenie.findAll().spliterator(), false)
                .filter(prietenie -> prietenie.getNodPrietenie1().equals(user.getId()) ||
                        prietenie.getNodPrietenie2().equals(user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void addObserver(Observer<PrietenieEntityChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<PrietenieEntityChangeEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(PrietenieEntityChangeEvent t) {
        observers.stream().forEach(x->x.update(t));
    }

    public boolean sendMessage(Utilizator user1, Utilizator user2, String message){
        List<Utilizator> to = new ArrayList<>();
        to.add(user2);
        List<Message> conversation = getMessagesBetweenUsers(user1, user2);
        List<Message> messagesFromUser2 = conversation.stream()
                .filter(msg -> msg.getFrom().equals(user2))
                .collect(Collectors.toList());
        Message lastMsgFromUser2 = messagesFromUser2.isEmpty() ? null : messagesFromUser2.get(messagesFromUser2.size() - 1);
        Message m = new Message(user1, to , message, LocalDateTime.now());
        if (lastMsgFromUser2 != null)
            m.setReply(lastMsgFromUser2);
        try {
            repoMessages.save(m);
            notifyObservers(new PrietenieEntityChangeEvent(ChangeEventType.MSG));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public List<Message> getMessagesBetweenUsers(Utilizator user1, Utilizator user2) {
        Iterable<Message> allMessages = repoMessages.findAll();

        List<Message> conversation = StreamSupport.stream(allMessages.spliterator(), false)
                .filter(message ->
                        (message.getFrom().equals(user1) && message.getTo().contains(user2)) ||
                                (message.getFrom().equals(user2) && message.getTo().contains(user1))
                )
                .sorted(Comparator.comparing(Message::getData)) // Sortăm mesajele în ordine cronologică
                .collect(Collectors.toList());
        //System.out.println(conversation);
        return conversation;
    }

    public String getLastFriendRequest(Utilizator user) {
        System.out.println("ASTA SE APELEAZA O DATA");
        Iterable<Prietenie> friendRequests = getFriendsRequest(user);
        List<Prietenie> unsolvedRequests = new ArrayList<>();
        for (Prietenie prietenie : friendRequests) {
            if (!prietenie.isNotificareTrimisa() && Objects.equals(prietenie.getStatus(), "PENDING") &&
                    Objects.equals(prietenie.getNodPrietenie1(), user.getId())) {

                unsolvedRequests.add(prietenie);
            }
        }
        if (!unsolvedRequests.isEmpty()) {
            unsolvedRequests.sort(Comparator.comparing(Prietenie::getFriendsFrom, Comparator.reverseOrder()));
            Prietenie lastRequest = unsolvedRequests.get(0);
            lastRequest.setNotificareTrimisa(true);
            System.out.println(lastRequest.isNotificareTrimisa());
            return repoUsers.findOne(lastRequest.getNodPrietenie2()).get().getUserName() +
                    " ți-a trimis o cerere de prietenie.";

        }
        return null;
    }

    public Message findMessageById(Long messageId) {
        Optional<Message> m = repoMessages.findOne(messageId);
        if(m.isPresent()){
            return m.get();
        }
        return null;
    }

    public boolean sendReplay(Utilizator user1, Utilizator user2, String message, Long userData) {
        List<Utilizator> to = new ArrayList<>();
        to.add(user2);
        Message m = new Message(user1, to , message, LocalDateTime.now());
        if (userData != null)
            m.setReply(findMessageById(userData));
        try {
            repoMessages.save(m);
            notifyObservers(new PrietenieEntityChangeEvent(ChangeEventType.MSG));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Page<Utilizator> findAllOnPage(Pageable pageable, FilterUserDTO filter) {
        Page<Prietenie> prietenii = repoPrietenie.findAllOnPage(pageable, filter);
        List<Utilizator> utilizatorOnPage = new ArrayList<>();
        prietenii.getElementsOnPage().forEach(x -> {
            if (x.getNodPrietenie1() == filter.getIdUser().get()) {
                utilizatorOnPage.add(findUtilizatoriById(x.getNodPrietenie2()));
            } else if (x.getNodPrietenie2() == filter.getIdUser().get()) {
                utilizatorOnPage.add(findUtilizatoriById(x.getNodPrietenie1()));
            }
        });
        return new Page<>(utilizatorOnPage, prietenii.getTotalNumberOfElements());
    }
}
