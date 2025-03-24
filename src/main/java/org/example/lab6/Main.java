package org.example.lab6;

import domain.Message;
import domain.Prietenie;
import domain.Utilizator;
import domain.validators.MessageValidator;
import domain.validators.PrietenieValidator;
import domain.validators.UtilizatorValidator;
import domain.validators.ValidationException;
import org.mindrot.jbcrypt.BCrypt;
import repository.Repository;
import repository.database.MessageDBRepository;
import repository.database.PrieteniDBRepository;
import repository.database.UtilizatorDBRepository;
import service.ServiceApp;

public class Main {
    public static void main(String[] args) {
        try {
//            Repository<Long,Utilizator> repoDBUtilizatori = new UtilizatorDBRepository(new UtilizatorValidator(),"jdbc:postgresql://localhost:5432/network", "postgres","postgres21");
//            Repository<Long, Prietenie> repoDBPrietenie = new PrieteniDBRepository(new PrietenieValidator(), "jdbc:postgresql://localhost:5432/network", "postgres","postgres21");
//            Repository<Long, Message> repoDBMesaje = new MessageDBRepository("jdbc:postgresql://localhost:5432/network", "postgres","postgres21", new MessageValidator(), repoDBUtilizatori);
//            ServiceApp service = new ServiceApp(repoDBUtilizatori, repoDBPrietenie, repoDBMesaje);
//            repoDBPrietenie.findAll().forEach(System.out::println);
//            //service.sendMessage(repoDBUtilizatori.findOne(2L).get(), repoDBUtilizatori.findOne(4L).get(), "heei");
//            repoDBMesaje.findAll().forEach(System.out::println);
        } catch (IllegalArgumentException | ValidationException e) {
            System.out.println(e.getMessage());
        }
        System.out.println();
        HelloApplication.main(args);
        //System.out.println(BCrypt.hashpw("cccc", BCrypt.gensalt()));
    }
}