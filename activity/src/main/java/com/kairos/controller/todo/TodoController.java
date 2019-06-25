package com.kairos.controller.todo;

import com.kairos.constants.ApiConstants;
import com.kairos.enums.shift.TodoStatus;
import com.kairos.service.todo.TodoService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.Map;

/**
 * Created by pradeep
 * Created at 25/6/19
 **/
@RestController
@RequestMapping(ApiConstants.TODO)
public class TodoController {

    @Inject private TodoService todoService;

    @ApiOperation("Get All todo")
    @GetMapping
    public ResponseEntity<Map<String,Object>> getAllTodo(@PathVariable Long unitId){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,todoService.getAllTodo(unitId));
    }

    @ApiOperation("Update status Of Todo")
    @PutMapping("/{todoId}")
    public ResponseEntity<Map<String,Object>> updateTodoStatus(@PathVariable BigInteger todoId, @RequestParam TodoStatus status){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,todoService.updateTodoStatus(todoId,status));
    }
}
