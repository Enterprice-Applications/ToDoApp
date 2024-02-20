package lk.ijse.dep11.todo.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.todo.to.TaskTO;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.Valid;
import javax.validation.groups.Default;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@RestController
@RequestMapping("/api/v1/tasks")
@CrossOrigin
public class TaskHttpController {
    private final HikariDataSource pool;

    public TaskHttpController(){
        HikariConfig config = new HikariConfig();
        config.setUsername("postgres");
        config.setPassword("root");
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/dep11_todo_app");
        config.setDriverClassName("org.postgresql.Driver");
        config.addDataSourceProperty("maximumPoolSize",10);
        pool = new HikariDataSource(config);
    }

   /* @PostConstruct
    public void initialize(){
        System.out.println("I am being created");
    }*/

    @PreDestroy
    public void destroy(){
        pool.close();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json", consumes = "application/json")
    public TaskTO createTask(@RequestBody @Validated(TaskTO.Create.class) TaskTO task){
       /* if(task.getId() != null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id should be empty");
        }else if (task.getDescription() == null || task.getDescription().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description can't be empty");
        } else if (task.getStatus() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status should be empty");
        }*/
       try(Connection connection = pool.getConnection()){

        PreparedStatement stm = connection.prepareStatement("INSERT INTO task (description, status, email) VALUES (?, FALSE, ?)",
               Statement.RETURN_GENERATED_KEYS);
        stm.setString(1, task.getDescription());
        stm.setString(2, task.getEmail());
        stm.executeUpdate();
           ResultSet generatedKeys = stm.getGeneratedKeys();
           generatedKeys.next();
           int id = generatedKeys.getInt(1);
           task.setId(id);
           task.setStatus(false);
           return task;
       }catch (SQLException e){
           throw new RuntimeException(e);
       }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(value ="/{id}", consumes = "application/json") // {id} path variable -> because id is dynamic it may change each requesting
    public void updateTask(@PathVariable("id") int taskID,
                           @RequestBody @Validated({TaskTO.Update.class}) TaskTO taskUpdate){
       try(Connection connection = pool.getConnection()){
           PreparedStatement stmExist = connection.prepareStatement("SELECT * FROM task WHERE id = ?");
           stmExist.setInt(1,taskID);
           if(!stmExist.executeQuery().next()){
               throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task Not Found");
           }
           PreparedStatement stm = connection.prepareStatement("UPDATE task SET description = ?, status = ? WHERE id=?");
           stm.setString(1, taskUpdate.getDescription());
           stm.setBoolean(2, taskUpdate.getStatus());
           stm.setInt(3, taskID);
           stm.executeUpdate();

       }catch (SQLException e){
           throw new RuntimeException(e);
       }
    }

    /* @ResponseStatus(HttpStatus.Ok */
    @GetMapping(produces = "application/json", params = {"email"})
    public List<TaskTO> getAllTasks(String email) {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM task WHERE email =? ORDER BY id");
            stm.setString(1, email);
            ResultSet rst = stm.executeQuery();
            List<TaskTO> taskList = new LinkedList<>();
            while (rst.next()) {
                int id = rst.getInt("id");
                String description = rst.getString("description");
                boolean status = rst.getBoolean("status");
                taskList.add(new TaskTO(id, description, status, email));
            }
            return taskList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable int id){
        try(Connection connection = pool.getConnection()){
            PreparedStatement stmExist = connection.prepareStatement("SELECT * FROM task WHERE id = ?");
            stmExist.setInt(1,id);
            if(!stmExist.executeQuery().next()){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task Not Found");
            }
            PreparedStatement stm = connection.prepareStatement("DELETE FROM task WHERE id=?");
            stm.setInt(1, id);
            stm.executeUpdate();

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

}
