package ru.saidgadjiev.orm.next.core.dao;

import ru.saidgadjiev.orm.next.core.stament_executor.GenericResults;
import ru.saidgadjiev.orm.next.core.stament_executor.result_mapper.ResultsMapper;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface BaseDao {

    <T> int create(Collection<T> objects) throws SQLException;

    /**
     * Метод сохраняет объект в базе
     * @param object создаваемый объект
     * @return количество созданных объектов. В данном случае 1
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    <T> int create(T object) throws SQLException;

    <T> boolean createTable(Class<T> tClass, boolean ifNotExists) throws SQLException;

    /**
     * Метод получает объект по id
     * @param id целевой id объекта
     * @return возвращает объект с заданной id или null
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    <T, ID> T queryForId(ID id, Class<T> tClass) throws SQLException;

    /**
     * Метод получает все объекты из таблицы T
     * @return все записи из таблицы типа T
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    <T> List<T> queryForAll(Class<T> tClass) throws SQLException;

    /**
     * Метод обновляет объект в базе
     * @param object обновляемый объект
     * @return количество обновленных объектов. В данном случае 1
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    <T> int update(T object) throws SQLException;

    /**
     * Метод удаляет запись из базы
     * @param object удаляемый объект
     * @return количество удаленных объектов. В данном случае 1
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    <T> int delete(T object) throws SQLException;

    <T, ID> int deleteById(ID id, Class<T> tClass) throws SQLException;

    <T> boolean dropTable(Class<T> tClass, boolean ifExists) throws SQLException;

    <T> void createIndexes(Class<T> tClass) throws SQLException;

    <T> void dropIndexes(Class<T> tClass) throws SQLException;

    <T> long countOff(Class<T> tClass) throws SQLException;

    <R> GenericResults<R> query(String query, ResultsMapper<R> resultsMapper) throws SQLException;

}
