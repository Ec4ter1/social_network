package service;



import domain.Prietenie;
import domain.Utilizator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Comunitati {
    private final ServiceApp service;

    public Comunitati(ServiceApp service) {
        this.service = service;
    }

//    private Map<Long, List<Long>> GrafPrietenii() {
//        Map<Long, List<Long>> graf = new HashMap<>();
//
//        for (Utilizator Utilizator : service.repoUtilizators.findAll()) {
//            graf.putIfAbsent(Utilizator.getId(), new ArrayList<>());
//        }
//        for (Prietenie prietenie : service.repoPrietenie.findAll()) {
//            graf.get(prietenie.getNodPrietenie1()).add(prietenie.getNodPrietenie2());
//            graf.get(prietenie.getNodPrietenie2()).add(prietenie.getNodPrietenie1());
//        }
//        return graf;
//    }
//
//    private void DFS(Long UtilizatorId, Set<Long> vizitat, Map<Long, List<Long>> graf, List<Long> componentaSocial) {
//        vizitat.add(UtilizatorId);
//        componentaSocial.add(UtilizatorId);
//        for (Long prietenId : graf.get(UtilizatorId)) {
//            if (!vizitat.contains(prietenId)) {
//                DFS(prietenId, vizitat, graf, componentaSocial);
//            }
//        }
//    }
//
//    public Long numarComunitati() {
//        Map<Long, List<Long>> graf = GrafPrietenii();
//        Set<Long> vizitat = new HashSet<>();
//        long nrComunitati = 0L;
//        for (Long UtilizatorId : graf.keySet()) {
//            if (!vizitat.contains(UtilizatorId)) {
//                DFS(UtilizatorId, vizitat, graf, new ArrayList<>());
//                nrComunitati += 1L;
//            }
//        }
//        return nrComunitati;
//    }
//
//    private Long[] bfs(Long start, Map<Long, List<Long>> graf) {
//        Queue<Long> queue = new LinkedList<>();
//        queue.add(start);
//        Map<Long, Integer> distanta = new HashMap<>();
//        distanta.put(start, 0);
//        Long distantat = start;
//        int maxDistanta = 0;
//        while (!queue.isEmpty()) {
//            Long UtilizatorCurent = queue.poll();
//            int distantaCurenta = distanta.get(UtilizatorCurent);
//            for (Long vecinId : graf.get(UtilizatorCurent)) {
//                if (!distanta.containsKey(vecinId)) {
//                    queue.add(vecinId);
//                    distanta.put(vecinId, distantaCurenta + 1);
//                }
//                if (distanta.get(vecinId) > maxDistanta) {
//                    maxDistanta = distanta.get(vecinId);
//                    distantat = vecinId;
//                }
//            }
//        }
//        return new Long[]{distantat, (long) maxDistanta};
//    }
//
//    public List<Long> comunitateSociabila() {
//        Map<Long, List<Long>> graf = GrafPrietenii();
//        Set<Long> vizitat = new HashSet<>();
//        int maxDrum = 0;
//        List<Long> sociabil = new ArrayList<>();
//        for (Long UtilizatorId : graf.keySet()) {
//            if (!vizitat.contains(UtilizatorId)) {
//                List<Long> compoentaSociala = new ArrayList<>();
//                DFS(UtilizatorId, vizitat, graf, compoentaSociala);
//
//                Long startUtilizator = compoentaSociala.get(0);
//                Long[] BFSOne = bfs(startUtilizator, graf);
//                Long[] BFSTwo = bfs(BFSOne[0], graf);
//                int lungime = BFSTwo[1].intValue();
//                if (maxDrum < lungime) {
//                    maxDrum = lungime;
//                    sociabil = compoentaSociala;
//                }
//            }
//        }
//        return sociabil;
//    }

    public int numarComunitati() {
        Iterable<Utilizator> it = service.repoUsers.findAll();
        Set<Utilizator> set = new HashSet<>();
        AtomicInteger count = new AtomicInteger(0);

        it.forEach(u -> {
            if (!set.contains(u)) {
                count.getAndIncrement();
                DFS(u, set, service::findUtilizatoriById);
            }
        });
        return count.get();

    }


    private List<Utilizator> DFS(Utilizator u, Set<Utilizator> set, Function<Long, Utilizator> findUserById) {
        List<Utilizator> list = new ArrayList<>();
        list.add(u);
        set.add(u);

        List<Long> listaPrieteniIds = u.getFriends();
        List<Utilizator> listaPrieteni = listaPrieteniIds.stream()
                .map(findUserById)
                .toList();

        listaPrieteni.forEach(f -> {
            if (!set.contains(f)) {
                List<Utilizator> l = DFS(f, set, findUserById);
                list.addAll(l);
            }
        });

        return list;
    }

    public Iterable<Iterable<Utilizator>> comunitateSociabila() {
        List<Iterable<Utilizator>> list = new ArrayList<>();
        Iterable<Utilizator> it = service.repoUsers.findAll();
        Set<Utilizator> set = new HashSet<>();

        final int[] max = {-1};

        it.forEach(u -> {
            if (!set.contains(u)) {
                List<Utilizator> aux = DFS(u, set, service::findUtilizatoriById);
                int l = longestPath(aux);
                if (l > max[0]) {
                    list.clear();
                    list.add(aux);
                    max[0] = l;
                } else if (l == max[0])
                    list.add(aux);
            }
        });

        return list;
    }

    private int longestPath(List<Utilizator> nodes) {
        final int[] max = {0};
        nodes.forEach(u -> {
            int l = longestPathFromSource(u);
            if (max[0] < l)
                max[0] = l;
        });
        return max[0];
    }

    private int longestPathFromSource(Utilizator source) {
        Set<Utilizator> set = new HashSet<>();
        return BFS(source, set, service::findUtilizatoriById);
    }

    private int BFS(Utilizator source, Set<Utilizator> set, Function<Long, Utilizator> findUserById) {
        final int[] max = {-1};

        List<Long> cIds = source.getFriends();
        List<Utilizator> c = cIds.stream()
                .map(findUserById)
                .toList();

        c.forEach(f -> {
            if (!set.contains(f)) {
                set.add(f);
                int l = BFS(f, set, findUserById);
                if (l > max[0])
                    max[0] = l;
                set.remove(f);
            }
        });
        return max[0] + 1;
    }

}
