package rest.felix.back.repository;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import rest.felix.back.dto.internal.CreateTodoDTO;
import rest.felix.back.dto.internal.TodoDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.Todo;
import rest.felix.back.entity.User;

import java.util.List;

@Repository
public class TodoRepository {

    private final EntityManager em;

    @Autowired
    public TodoRepository(EntityManager em) {
        this.em = em;
    }

    public List<TodoDTO> getTodosInGroup(long groupId) {
        return em.createQuery("""
                        SELECT
                            t
                        FROM
                            Group g
                        JOIN
                            g.todos t
                        JOIN FETCH
                            t.author
                        WHERE
                            g.id = :groupId
                        """, Todo.class)
                .setParameter("groupId", groupId)
                .getResultList()
                .stream()
                .map(todo -> new TodoDTO(todo.getId(), todo.getTitle(), todo.getDescription(), todo.getTodoStatus(), todo.getAuthor().getId(), groupId))
                .toList();
    }


    public TodoDTO createTodo(CreateTodoDTO createTodoDTO) {
        Todo todo = new Todo();

        User author = em.getReference(User.class, createTodoDTO.getAuthorId());
        Group group = em.getReference(Group.class, createTodoDTO.getGroupId());

        todo.setAuthor(author);
        todo.setGroup(group);
        todo.setTitle(createTodoDTO.getTitle());
        todo.setDescription(createTodoDTO.getDescription());

        em.persist(todo);

        return new TodoDTO(todo.getId(), todo.getTitle(), todo.getDescription(), todo.getTodoStatus(), todo.getAuthor().getId(), todo.getGroup().getId());
    }
}
