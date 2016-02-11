package main;

import main.commands.LoadCommand;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        new Main();
    }

    public Integer currentTurn = 0;
    public Integer rows;
    public Integer columns;
    public Integer turns;
    public List<Drone> drones = new ArrayList<>();
    public List<Warehouse> warehouses = new ArrayList<>();
    public List<Order> orders = new ArrayList<>();
    public String FILE_NAME = "busy_day.in";

    public Main() {
        init();
    }

    private void init() {
        readFile();
    }

    public void execution() {
        while (incompleteOrders() && currentTurn < turns) {
            Order bestOrder = getBestOrder();
            while (!bestOrder.isCompleted) {
                Drone closestAvailableDrone = getClosestAvailableDrone(bestOrder.destination);
                Warehouse previous = null;
                for (Product order : bestOrder.orders) {
                    if (order.count > 0) {
                        Warehouse warehouse = getWarehouse(bestOrder.destination, order);
                        if(previous == null || warehouse == previous){
                            Integer weight = ProductWeights.getInstance().getWeight(order.type);
                            Integer limit = ProductWeights.getInstance().getWeightLimit();
                            Integer availableWeight = limit - closestAvailableDrone.getWeight();
                            Integer count = availableWeight / weight;
                            closestAvailableDrone.commands
                                    .add(new LoadCommand(warehouse,
                                            new Product(order.type, count),
                                            closestAvailableDrone));
                            closestAvailableDrone.execute();
                        }
                        previous = warehouse;
                    }
                }
            }
            currentTurn++;
        }
    }

    public boolean incompleteOrders(){
        for (Order order : orders) {
            for (Product product : order.orders) {
                if( product.count > 0){
                    return false;
                }
            }
        }
        return true;
    }

    public Warehouse getWarehouse(Position pos, Product product) {
        return getSortedWarehouses(pos, product).get(0);
    }

    private List<Warehouse> getSortedWarehouses(Position pos, Product product) {
        List<Warehouse> hasProduct = new ArrayList<>();
        for (Warehouse warehouse : warehouses) {
            if (warehouse.products.get(product.type).count >= product.count) {
                hasProduct.add(warehouse);
            }
        }
        hasProduct.sort((o1, o2) -> o1.location.distanceTo(pos).compareTo(o2.location.distanceTo(pos)));
        return hasProduct;
    }

    public Order getBestOrder() {
        List<Order> todoOrders = getTodoOrders();
        Position averageDronePosition = getDronesAvaragePosition();
        todoOrders.sort((o1, o2) -> o1.destination.distanceTo(averageDronePosition)
                .compareTo(o2.destination.distanceTo(averageDronePosition)));
        return todoOrders.get(0);
    }

    public List<Order> getTodoOrders() {
        List<Order> todo = new ArrayList<>();
        for (Order order : orders) {
            if (!order.isCompleted) {
                todo.add(order);
            }
        }
        return todo;
    }

    public List<Drone> getAvailableDrones() {
        List<Drone> availableDrones = new ArrayList<>();
        while (currentTurn < turns) {
            for (Drone drone : drones) {
                if (drone.turn <= currentTurn) {
                    availableDrones.add(drone);
                }
            }
            if (availableDrones.size() > 0) {
                return availableDrones;
            }
            currentTurn++;
        }
        return null;
    }

    public Position getDronesAvaragePosition() {
        int x = 0, y = 0;
        List<Drone> availableDrones = getAvailableDrones();
        for (Drone drone : availableDrones) {
            x += drone.position.coordX;
            y += drone.position.coordY;
        }
        return new Position(x / availableDrones.size(), y / availableDrones.size());
    }

    public Drone getClosestAvailableDrone(Position pos) {
        while (currentTurn < turns) {
            List<Drone> availableDrones = new ArrayList<>();
            for (Drone drone : drones) {
                if (drone.turn <= currentTurn) {
                    availableDrones.add(drone);
                }
            }
            if (availableDrones.size() > 0) {
                availableDrones.sort((o1, o2) -> o1.position.distanceTo(pos).compareTo(o2.position.distanceTo(pos)));
                return availableDrones.get(0);
            }
            currentTurn++;
        }
        return null;
    }


    public void readFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));

            Scanner scanner = new Scanner(br);

            this.rows = scanner.nextInt();
            this.columns = scanner.nextInt();
            int droneCount = scanner.nextInt();
            this.turns = scanner.nextInt();

            ProductWeights.getInstance().setWeightLimit(scanner.nextInt());
            scanner.nextLine();
            int productTypesCount = scanner.nextInt();
            scanner.nextLine();
            for (int i = 0; i < productTypesCount; i++)
                ProductWeights.getInstance().addWeight(scanner.nextInt());

            scanner.nextLine();
            int warehousesCount = scanner.nextInt();
            for (int i = 0; i < warehousesCount; i++) {
                scanner.nextLine();
                int x, y;
                x = scanner.nextInt();
                y = scanner.nextInt();
                if (i == 0) {
                    for (int j = 0; j < droneCount; j++) {
                        drones.add(new Drone(j, new Position(x, y)));
                    }
                }
                scanner.nextLine();
                Warehouse warehouse = new Warehouse(i, new Position(x, y));
                for (int j = 0; j < productTypesCount; j++) {
                    warehouse.products.add(new Product(j, scanner.nextInt()));
                }
                warehouses.add(warehouse);
            }
            scanner.nextLine();
            int ordersCount = scanner.nextInt();
            scanner.nextLine();
            for (int i = 0; i < ordersCount; i++) {
                int x, y;
                x = scanner.nextInt();
                y = scanner.nextInt();
                scanner.nextLine();
                int orderProductCount = scanner.nextInt();
                scanner.nextLine();
                Order temp = new Order(i, new Position(x, y));
                for (int j = 0; j < orderProductCount; j++) {
                    temp.orders.add(new Product(j, scanner.nextInt()));
                }
                orders.add(temp);
                scanner.nextLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
