package ca.camosun.ICS226;
//java -jar target/*.jar
//mvn package
//nc localhost port
//http://10.21.75.76:8080/test/10.21.75.92
import java.net.*;
import java.io.*;
public class Server {
    protected int port;
    static final char GRID= 'G';
    static final char PUT = 'P';
    static final char CLEAR = 'C';
    static final String SUCCESS = "O";
    static final String ERROR = "E";
    static final int COMMAND = 0;
    static final int BOARD_SIZE = 4;
    static final int MAX_PLAYERS = 4;
    static final String EMPTY = "_";
    static final String TERMINATOR = "*";
    static final int ROW = 2;     //Row position  in the user input string array
    static final int LEVEL = 1;//Level position  in the user input string array
    static final int COLUMN = 3; //Column position  in the user input string array
    final static int MOVE = 4; //Move position  in the user input string array
    static String[][][] grid = new String[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
    static int playerCount = -1;
    static int turn = 1;
    static boolean gameover = false;
    static int winningPlayer = -1;
    public Server(int port) {
        this.port = port;
    }
    //Checks for the winning condition
    public static void checkWinCondition(int id){
        int horizontalWinCount;
        for(int i = 0; i < BOARD_SIZE; i++){
            for(int j = 0; j < BOARD_SIZE; j++){
                horizontalWinCount = 0;
                for (int k = 0; k < BOARD_SIZE; k++){
                    if(grid[i][j][k] != "_")
                    {
                        if(Integer.parseInt(grid[i][j][k]) == id)
                        {
                            horizontalWinCount++;
                            if(horizontalWinCount == 4){
                                gameover = true;
                                winningPlayer = id;
                                break;
                            }
                        }
                    }
                    
                }
            }
        }
    }
    //generates the board
    public static String getGridString(){
        String gridString = new String();
        for(int i = 0; i < BOARD_SIZE; i++){
          
            for(int j = 0; j < BOARD_SIZE; j++){
              
                for (int k = 0; k < BOARD_SIZE; k++){
                    gridString += grid[i][j][k];
                }
                gridString += "\n";
            }
            gridString += "\n";
        }
        return gridString;
    }
    //clears the board
    public static void clearBoard(){
        gameover = false;
        winningPlayer = -1;
        turn = 1;
        for(int i = 0; i < BOARD_SIZE; i++){
            for(int j = 0; j < BOARD_SIZE; j++){
                for (int k = 0; k < BOARD_SIZE; k++){
                    grid[i][j][k] = EMPTY;  
                }
            }
        }
    }
    //changes turns when a move is successfully made. Turn will depend on the max players
    public static void changeTurn(){
        turn++;
        if(turn >= MAX_PLAYERS){
            turn = 1;
        }        
    }
    //check if it's the player's turn. 
    public static boolean checkTurn(int id){
        return (id == turn);
    }
    //Adds a move to the board.
    public static String addToBoard(int level, int row, int col, int id){
        if(level < BOARD_SIZE && row < BOARD_SIZE && col < BOARD_SIZE && checkTurn(id)){
            if(grid[level][row][col] == "_"){
                grid[level][row][col] = Integer.toString(id);
                checkWinCondition(id);
                changeTurn();
                return "O";
            }
            return "E";
        }
        return "E";
    }
    //validation for a PUT command. 
    public static String makeMove(char[] userInput){
        int move = Character.getNumericValue(userInput[MOVE]);
        if(Character.isDigit(userInput[LEVEL]) && 
        Character.isDigit(userInput[ROW]) && 
        Character.isDigit(userInput[COLUMN]) && 
        move == turn &&
        winningPlayer < 0){
            int level = Character.getNumericValue(userInput[LEVEL]);
            int row = Character.getNumericValue(userInput[ROW]);
            int col = Character.getNumericValue(userInput[COLUMN]);
            return  addToBoard(level, row, col, move);
        }
        else{
            return  "E";
        }
    }
    void delegate(Socket clientSocket) {
        try (
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            playerCount++;
            //Assigns the new player with the playerCount global variable as the new player's player ID
            int playerId = playerCount;
            while (true) {
                String inputLine = in.readLine();
                char[] userInput = inputLine.toCharArray();
                synchronized(this) {
                    System.out.println(inputLine);
                }
                String returnData = new String();
                if (playerId >= MAX_PLAYERS)
                {
                    returnData = "R";
                }
                else if(userInput[COMMAND] == GRID){
                    //Shows the grid if G is entered.
                    checkTurn(playerId);
                    returnData = getGridString();
                    if(winningPlayer < 0){
                        returnData += "Player " + Integer.toString(turn)  + "'s turn";
                    }
                    else{
                        returnData += "Player " + Integer.toString(winningPlayer)  + " wins";
                    }
                    
                }
                else if(userInput[COMMAND] == PUT){
                    //Puts move on the board if P is entered along with valid numbers within BOARD_SIZE
                    if(inputLine.length() < 5 || winningPlayer > 0){
                        returnData = "E";
                    }
                    else {
                        returnData = makeMove(userInput);
                        
                    }
                }
                else if(userInput[COMMAND] == CLEAR){
                    //Clears the board
                    clearBoard();
                    returnData = "O";
                }
                else{
                    returnData = "E";
                }
                //Terminates each print with *
                out.print(returnData + TERMINATOR);
                out.flush();
            }
            //clientSocket.close();
        } 
        catch (Exception e) {
            System.err.println(e);
            System.exit(-1);
        }
    }
    public void serve() {
        try (
        ServerSocket serverSocket = new ServerSocket(port);
        ) {
            while(true) {
                try {
                    Socket clientSocket = serverSocket.accept();Runnable runnable = () -> this.delegate(clientSocket);
                    Thread t = new Thread(runnable);
                    t.start();
                } catch (Exception e) {
                    System.err.println(e);
                    System.exit(-2);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            System.exit(-3);
        }
    }

    public static void main(String[] args) {
        clearBoard();
        playerCount ++;
        Server s = new Server(12345);
        s.serve();
    }
}

