package de.reondo.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static akka.actor.ActorRef.noSender;

/**
 * Created by denni on 04/11/2017.
 */
public class AkkaApp {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ActorSystem system = ActorSystem.create();

        try {
            ActorRef rootActor = system.actorOf(RootActor.props());
            rootActor.tell(new RootActor.Start(), noSender());
            Scanner input = new Scanner(System.in);
            while (input.hasNextLine()) {
                String line = input.nextLine();
                if ("exit".equals(line)) {
                    break;
                } else {
                    rootActor.tell(line, noSender());
                }
            }
        }
        finally {
            system.terminate();
        }
    }

}
