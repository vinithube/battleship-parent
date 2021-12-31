package com.odigeo.interview.coding.battleshipservice.util;

import com.odigeo.interview.coding.battleshipservice.exception.ShipDeploymentException;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.model.ship.ShipType;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Singleton
public class ShipDeploymentValidator {

    public void validate(List<Ship> ships) {
        numberOfDeployedShips(ships);
        duplicatedDeployment(ships);
        shipsOverlap(ships);
        ships.forEach(ship -> {
            shipLengthIsWrong(ship);
            shipIsOutOfGrid(ship);

            shipIsNotContiguous(ship);
            //shipContiguous(ship);
        });
    }

    private void duplicatedDeployment(List<Ship> ships) {
        List<String> deployedTypes = new ArrayList<>();
        for (Ship ship: ships) {
            String shipType = ship.getShipType().getShipTypeName();
            if (deployedTypes.contains(shipType)) {
                throw new ShipDeploymentException(shipType, ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
            }
            deployedTypes.add(shipType);
        }
    }

    private void numberOfDeployedShips(List<Ship> shipDeployments) {
        int requiredNumberOfDeployments = ShipType.values().length;
        int providedDeploymentsNumber = shipDeployments.size();
        if (providedDeploymentsNumber != requiredNumberOfDeployments) {
            throw new ShipDeploymentException(String.format("Wrong number of deployed ships: expected %d, got %d", requiredNumberOfDeployments, providedDeploymentsNumber));
        }
    }

    private void shipLengthIsWrong(Ship ship) {
        // Use a set to account for overlapping coordinates
        boolean isLengthWrong = new HashSet<>(ship.getCoordinates()).size() != ship.getShipType().getShipLength();
        if (isLengthWrong) {
            throw new ShipDeploymentException(ship.getShipType().getShipTypeName(), ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
        }
    }

    private void shipIsOutOfGrid(Ship ship) {
        boolean isShipOutOfGrid = ship.getCoordinates().stream().anyMatch(ShipDeploymentValidator::coordinateIsOutOfGrid);
        if (isShipOutOfGrid) {
            throw new ShipDeploymentException(ship.getShipType().getShipTypeName(), ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
        }
    }

    private static boolean coordinateIsOutOfGrid(Coordinate coordinate) {
        return coordinate.getRow() < 0 ||
                coordinate.getColumn() < 0 ||
                coordinate.getRow() >= GameConfiguration.FIELD_HEIGHT ||
                coordinate.getColumn() >= GameConfiguration.FIELD_WIDTH;
    }

    private void shipIsNotContiguous(Ship ship) {
        boolean shipIsNotContiguous = !isHorizontal(ship) && !isVertical(ship);
        if (shipIsNotContiguous) {
            throw new ShipDeploymentException(ship.getShipType().getShipTypeName(), ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
        }
    }



    private boolean isHorizontal(Ship ship) {
        List<Coordinate> coordinates = ship.getCoordinates();
        int firstRow = coordinates.get(0).getRow();



        boolean allRowMatched =  coordinates.stream().allMatch(c -> c.getRow() == firstRow);
        boolean isCloumAlign = validatHorizontalCloumn(coordinates);

        if(allRowMatched && isCloumAlign){
            return true;
        }
        return false;
    }

    private boolean validatHorizontalCloumn(List<Coordinate> coordinates) {
        int valueOfFirstClo = coordinates.get(0).getColumn();
        for(int i = 1 ; i< coordinates.size() ; i++ ){
                if(valueOfFirstClo + 1 != coordinates.get(i).getColumn()){

                    return false;
                }
            valueOfFirstClo = coordinates.get(i).getColumn();
          }
        return true;
    }

    private boolean isVertical(Ship ship) {
        List<Coordinate> coordinates = ship.getCoordinates();
        int firstColumn = coordinates.get(0).getColumn();

        boolean allCloumMatched =  coordinates.stream().allMatch(c -> c.getColumn() == firstColumn);
        boolean allVerticalRowsMatched = validatVerticalRows(coordinates);

        if(allCloumMatched && allVerticalRowsMatched){
            return true;
        }
        return false;
    }

    private boolean validatVerticalRows(List<Coordinate> coordinates) {
        int valueOfFirstRow = coordinates.get(0).getRow();
        for(int i = 1 ; i< coordinates.size() ; i++ ){
            if(valueOfFirstRow + 1 != coordinates.get(i).getRow()){
                return false;
            }
            valueOfFirstRow = coordinates.get(i).getRow();
        }
        return true;
    }

    private void shipsOverlap(Collection<Ship> deployedShips) {
        List<Coordinate> allCoordinates = deployedShips.stream()
                .flatMap(ship -> ship.getCoordinates().stream())
                .collect(toList());
        Set<Coordinate> reducedCoordinates = new HashSet<>(allCoordinates);
        boolean isOverlapping = allCoordinates.size() != reducedCoordinates.size();
        if (isOverlapping) {
            throw new ShipDeploymentException("Deployed ships overlap - they cannot overlap!");
        }
    }
}
