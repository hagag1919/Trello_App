package service;

import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;

import model.Board;
import model.Card;
import model.ListofCards;
import model.User;

@Stateless
public class ListService {
	@PersistenceContext
	private EntityManager entityManager;

//	@Inject
//    private MessagingSystemService messagingService;

	// create lists within a board to categorize tasks.
	// Assuming you have a method to create a new ListofCards and associate it with a Board
	public Response createList(String listName, Long boardId , Long userId) {
	    Board board = entityManager.find(Board.class, boardId);
	    if (board == null) {
	    	return Response.status(Response.Status.NOT_FOUND).entity("Board not found with id: ").build();
	    }
	    User user = entityManager.find(User.class, userId);
		if (user == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
		}
		// check if user is a member of the board
		if (!board.getContributors().contains(user)) {
					return Response.status(Response.Status.UNAUTHORIZED).entity("User is not a member of the board").build();
		}
	    ListofCards newList = new ListofCards(listName);
	    newList.setBoard(board); // Set the board for the new list
	    entityManager.persist(newList); // Persist the new list
	    // Add the new list to the board's list of cards
	    board.getLists().add(newList);

	    return Response.ok(newList).build();
	}


	// delete list from board using user id
	public Response deleteList(Long listId, Long teamLeaderId) {
		// check if list exists
		ListofCards list = entityManager.find(ListofCards.class, listId);
		if (list == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("List not found").build();
		}
		// check if user exists
		User user = entityManager.find(User.class, teamLeaderId);
		if (user == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
		}
		// check if user is a teamLeader of the board
		Board board = list.getBoard();
		if (!board.getTeamLeader().equals(user)) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("User is not a team leader of the board")
					.build();
		}
		entityManager.remove(list);
		return Response.ok("List deleted successfully").build();
	}

}
