package Client;

import Client.ClientReceive;
import Common.FileHelpers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Client {
    
    private static final int maxDebt = 500000;
    
    public static void main(String[] args) throws Exception{
        
        File directory = new File("ClientFiles/");
        if (! directory.exists()){
            directory.mkdir();
        }
        
        //Socket conectado na porta 12345 e com o IP 127.0.0.1 (localhost) **/
        Socket socket = new Socket(InetAddress.getLocalHost(), 12345);

        // Pipes to communicate with thread responsible to deal with server messages
        PipedOutputStream output = new PipedOutputStream();
        PipedInputStream  input  = new PipedInputStream(output);
        PrintWriter pipeWriter = new PrintWriter(output);
        
        // in le do input do pipe **/
        BufferedReader in = new BufferedReader(new InputStreamReader(input));       
        // buffer vai ler do System.in **/
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        // out escreve no output do socket **/
        PrintWriter out = new PrintWriter((socket.getOutputStream()));
        
        Thread receive = new Thread(new ClientReceive(socket, pipeWriter));
        receive.start();
        boolean login = false;
        menuLogin();
        String s;
        while(true && (s = buffer.readLine()) != null){            
            if(s.equals("3") && !login)         // Se o cliente escreve Quit o cliente fecha **/
            {
                break;
            }
            else if(s.equals("4") && login)
            {
                out.println("logout");
                out.flush();
                
                String serverResponse = in.readLine();
                if(serverResponse.equals("OK"))
                {
                    login = false;
                    clearMenu();
                    menuLogin();
                    continue;
                }
            }
            
            clearMenu();
            if(!login)
            {
                switch(s)
                {
                    case "1":
                    {
                        System.out.println("Insira o nome de utilizador");
                        String username = buffer.readLine();
                        System.out.println("Insira a password");
                        String password = buffer.readLine();

                        out.println("add user " + username + " " + password);
                        out.flush();

                        
                        String serverResponse = in.readLine();
                        if(serverResponse.equals("OK"))
                        {
                            System.out.println("Utilizador registado com sucesso. Pressione qualquer tecla para continuar.");
                        }
                        else
                        {
                            System.out.println("Já existe um utilizador com esse nome. Pressione qualquer tecla para voltar.");
                        }
                        break;
                    }
                    case "2":
                    {
                        System.out.println("Insira o nome de utilizador");
                        String username = buffer.readLine();
                        System.out.println("Insira a password");
                        String password = buffer.readLine();

                        out.println("login " + username + " " + password);
                        out.flush();

                        String serverResponse = in.readLine();
                        if(serverResponse.equals("OK"))
                        {
                            login = true;
                            System.out.println("Login efetuado. Pressione qualquer tecla para continuar.");
                        }
                        else
                        {
                            login = false;
                            System.out.println("Login errado. Pressione qualquer tecla para voltar.");
                        }
                        break;
                    }
                    default:
                    {
                        System.out.println("Invalid command.");
                        break;
                    }
                }
            }
            else
            {
                String response = executeMenu(s, buffer, in, out);
                if(response.equals("OK 1"))
                {
                    System.out.println("Musica adicionada com sucesso. Pressione qualquer tecla para continuar.");
                } 
                else if(response.equals("OK 3"))
                {
                    System.out.println("Musica descarregada com sucesso. Pressione qualquer tecla para continuar.");
                } 
                else if(response.equals("listEnd")) 
                {
                    System.out.println("\nPressione qualquer tecla para continuar.");
                } 
                else if(response.equals("NoMatch"))
                {
                    System.out.println("Não foi encontrada nenhuma música.Pressione qualquer tecla para continuar.");
                }
                else if(response.equals("Error 1"))
                {
                    System.out.println("Utilizador inválido. Pressione qualquer tecla para voltar.");
                } 
                else if(response.equals("Error 2"))
                {
                    System.out.println("Erro de IO, por favor tente novamente. Pressione qualquer tecla para voltar.");
                }
                else if(response.equals("Error 3"))
                {
                    System.out.println("Musica inexistente. Pressione qualquer tecla para voltar.");
                } 
                else if(response.equals("Error 4"))
                {
                    System.out.println("Operação inválida. Pressione qualquer tecla para voltar.");
                } 
                else if(response.equals("Error 5"))
                {
                    System.out.println("Erro ao transferir ficheiro. Pressione qualquer tecla para voltar.");
                } 
                else if(response.equals("Error 6"))
                {
                    System.out.println("Utilizador já existente. Pressione qualquer tecla para voltar.");
                }
                else if(response.equals("Error 7"))
                {
                    System.out.println("Ficheiro nao encontrado. Pressione qualquer tecla para voltar.");
                } 
                else if(response.equals("Error 8"))
                {
                    System.out.println("Ficheiro invalido. Pressione qualquer tecla para voltar.");
                } 
                else
                {
                    System.out.println("Erro.");
                }
            }
              
            buffer.readLine();
            clearMenu();
            if(!login)
            {
                menuLogin();
            }
            else
            {
                mainMenu();
            }
        }
        
        socket.shutdownOutput();                // Fecha o lado de escrita do socket **/
        socket.shutdownInput();                 // Fecha o lado de leitura do socket **/
        socket.close();                         // Fecha o socket **/
        
        receive.join();
        System.out.println("Done");
    }
    
    
    private static void menuLogin()
    {
        System.out.println("1 - Registar utilizador");
        System.out.println("2 - Login");
        System.out.println("3 - Sair");
    }
    
    private static void mainMenu()
    {
        System.out.println("1 - Adicionar musica");
        System.out.println("2 - Pesquisar musica");
        System.out.println("3 - Descarregar musica");
        System.out.println("4 - Logout");
    }
    
    private static String executeMenu(String s, BufferedReader buffer, BufferedReader in, PrintWriter out)
    {
        switch(s)
        {
            case "1":
            {
                try {
                    System.out.println("Insira nome da musica");
                    String name = buffer.readLine();
                    System.out.println("Insira o artista da musica");
                    String artist = buffer.readLine();
                    System.out.println("Insira ano da musica");
                    String year = buffer.readLine();
                    
                    boolean yearValid = false;
                    while(!yearValid)
                    {
                        try
                        {
                           Integer.parseInt(year); 
                            yearValid = true;
                        }
                        catch(Exception ex)
                        {
                            System.out.println("Ano inválido, insira novamente.");
                            year = buffer.readLine();
                        }
                    }
                    
                    System.out.println("Insira as etiquetas separadas por espaços");
                    String labels = buffer.readLine();
                    
                    System.out.println("Insira o caminho do ficheiro");
                    //File currentDir = new File("");
                    //System.out.println("Current Directory : " + currentDir.getAbsoluteFile());
                    String filepath = buffer.readLine();
                    
                    String[] filepathSplit = filepath.split("\\.");
                    if(filepathSplit.length <=1)
                    {
                        return "Error 7";
                    }
                    
                    String extension = filepathSplit[filepathSplit.length -1];
                    
                    out.println("add music " + name + " " + artist + " " + year + " " + extension + " " + labels);
                    out.flush();
                    
                    out.println("StartFile");
                    out.flush();
                    File uploadFile = new File(filepath);
                    long fileLength = uploadFile.length();
                    
                    for(int i=0; i<fileLength; i+= maxDebt)
                    {
                        String sendingMessage = FileHelpers.bytesToHex(FileHelpers.toByteArray(uploadFile, i, maxDebt));
                        out.println(sendingMessage);
                        out.flush();
                    }
                    
                    out.println("EndFile");
                    out.flush();
                    
                    String response = in.readLine();
                    
                    if(response.equals("OK"))
                    {
                        return "OK 1";
                    }
                    
                    return response;
                    
                } catch (IOException ex) {
                    return "Error 2";
                }                
            }
            case "2":
            {
                try {
                    int option=0;
                    while(option<1 || option>6) 
                    {
                        System.out.println("Selecione uma opção de pesquisa");
                        System.out.println("1: Id");
                        System.out.println("2: Título");
                        System.out.println("3: Intérprete");
                        System.out.println("4: Ano");
                        System.out.println("5: Etiquetas");
                        System.out.println("6: Downloads");
                        option = (Integer) parseInt(buffer.readLine());
                    }           
                    switch(option) 
                    {
                        case 1:
                        {
                            System.out.println("Insira o id");
                            String id = buffer.readLine();
                            out.println("ListMusics id " + id);
                            out.flush();
                            break;
                        }
                        case 2:
                        {
                            System.out.println("Insira o título");
                            String title = buffer.readLine();
                            out.println("ListMusics title " + title);
                            out.flush();
                            break;
                        }
                        case 3:
                        {
                            System.out.println("Insira o intérprete");
                            String author = buffer.readLine();
                            out.println("ListMusics author " + author);
                            out.flush();
                            break;
                        }
                        case 4:
                        {
                            System.out.println("Insira o ano");
                            String year = buffer.readLine();
                    
                            boolean yearValid = false;
                            while(!yearValid)
                            {
                                try
                                {
                                Integer.parseInt(year); 
                                    yearValid = true;
                                }
                                catch(Exception ex)
                                {
                                    System.out.println("Ano inválido, insira novamente.");
                                    year = buffer.readLine();
                                }
                            }
                            out.println("ListMusics year " + year);
                            out.flush();
                            break;
                        }
                        case 5:
                        {
                            System.out.println("Insira as etiquetas separadas por espaços");
                            String labels = buffer.readLine();
                            out.println("ListMusics labels " + labels);
                            out.flush();
                            break;
                        }
                        case 6: 
                        {
                            System.out.println("Insira o número minimo de downloads");
                            String minDownloads = buffer.readLine();
                            out.println("ListMusics minDownloads " + minDownloads);
                            out.flush();
                            break;
                        }                    
                    }
                    String response = in.readLine();
                    if(response.equals("NoMatch"))
                    {
                        return response;
                    }
                    if(response.equals("musicList")) response = in.readLine();
                    while(!response.equals("listEnd"))
                    {
                        System.out.println(response);
                        response = in.readLine();
                    }
                    return response;
                } catch (IOException ex) {
                    return "Error 2";
                }
            }
            case "3":
            {
                try {
                    System.out.println("Insira id da musica");
                    String id = buffer.readLine();
                    
                    boolean idValid = false;
                    while(!idValid)
                    {
                        try
                        {
                           Integer.parseInt(id); 
                            idValid = true;
                        }
                        catch(Exception ex)
                        {
                            System.out.println("Id inválido, insira novamente.");
                            id = buffer.readLine();
                        }
                    }
                    
                    out.println("fetch music " + id);
                    out.flush();
                    
                    String response = in.readLine();
                    if(!response.startsWith("NameFile:"))
                    {
                        return response;
                    }

                    String fileName = response.replace("NameFile:", "");

                    String temp = in.readLine();

                    if(temp.startsWith("Error"))
                    {
                        return temp;
                    }
                    
                    if(temp.equals("StartFile"))
                    {
                        FileOutputStream downloadingFile;
                        downloadingFile = new FileOutputStream("ClientFiles/" + fileName);

                        while((temp = in.readLine()) != null && !temp.equals("EndFile"))
                        {
                            FileHelpers.writeBytesToFile(downloadingFile, temp);
                        }
                        downloadingFile.close();

                        if(temp.equals("EndFile"))
                        {
                            return "OK 3";
                        }
                        else
                        {
                            return "Error 5"; // Error while downloading file
                        }
                    }
                    else
                    {
                        return "Error 8"; // Invalid file
                    }


                } catch (IOException ex) {
                    return "Error 2";
                }
            }
            default:
                {
                    return "Error 4";
                }
        }
    }
    
    private static void clearMenu()
    {
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
    }
}
